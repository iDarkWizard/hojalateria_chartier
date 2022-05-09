package com.idarkwizard.calculatorapp.layout.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.idarkwizard.calculatorapp.R;
import com.idarkwizard.calculatorapp.layout.ListLayout;
import com.idarkwizard.calculatorapp.service.CalculationService;
import com.idarkwizard.calculatorapp.service.UtilService;
import com.idarkwizard.calculatorapp.layout.slider.SliderAdapter;
import com.idarkwizard.calculatorapp.layout.slider.SliderAdapterDefaultColor;
import com.idarkwizard.calculatorapp.layout.slider.SliderItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AbstractFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AbstractFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_TITLE_NAME = "title";

    private Map<String, String> data;
    private List<String> dataKeys;
    private String selectableKey;
    private String rootDataKey;
    private String title;

    private ViewPager2 viewPager2;
    private View actualView;
    private AtomicInteger selected;
    private AtomicInteger currentResult;
    private AtomicInteger resultsSize;
    private List<SliderItem> sliderItems;
    EditText width;
    EditText length;
    EditText quantity;
    TextView thickness;

    public AbstractFragment() {
        // Required empty public constructor
    }

    public AbstractFragment(String title) {
        this.title = title;
    }

    @SuppressLint("NewApi")
    public AbstractFragment(String title, Map<String, String> data) {
        this.title = title;
        this.data = data;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title Parameter 1.
     * @return A new instance of fragment plates.
     */
    // TODO: Rename and change types and number of parameters
    public static AbstractFragment newInstance(String title) {
        AbstractFragment fragment = new AbstractFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE_NAME, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null)
            return;
        title = getArguments().getString(ARG_TITLE_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        loadData();
        View view = loadLayouts(inflater, container);
        this.width = view.findViewById(R.id.width_edit_text);
        this.length = view.findViewById(R.id.length_edit_text);
        this.quantity = view.findViewById(R.id.quantity_edit_text);
        this.thickness = view.findViewById(R.id.thickness_input);
        return view;
    }

    private View loadLayouts(LayoutInflater inflater, ViewGroup container) {
        switch (rootDataKey) {
            case "forrar_mesa":
            case "bandeja":
                return inflater.inflate(R.layout.main_fragment_variant, container, false);
            default:
                return inflater.inflate(R.layout.main_fragment, container, false);
        }
    }

    private void loadData() {
        sliderItems = new ArrayList<>();
        selected = new AtomicInteger(-1);
        currentResult = new AtomicInteger(0);
        resultsSize = new AtomicInteger(0);
        this.rootDataKey = title.toLowerCase(Locale.ROOT)
                .replaceAll(" ", "_");
        loadDataMapByVersion(rootDataKey);
        this.dataKeys = UtilService.splitAsList(data.get(rootDataKey + "_names"));
    }

    private void loadDataMapByVersion(String key) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.data = data.entrySet()
                    .stream()
                    .filter(entry ->
                            entry.getKey()
                                    .contains(key))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            // DOUBLE CODE CAUSE ANDROID :)
            Map<String, String> auxDataMap = new HashMap<>();
            Set<Map.Entry<String, String>> dataEntrySet = data.entrySet();
            for (Map.Entry<String, String> entry : dataEntrySet) {
                if (entry.getKey()
                        .contains(key)) {
                    auxDataMap.put(entry.getKey(), entry.getValue());
                }
            }
            this.data = auxDataMap;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        viewPager2 = view.findViewById(R.id.results_view_pager);
        sliderItems.add(new SliderItem(getResources().getString(R.string.results_default)));
        viewPager2.setAdapter(new SliderAdapterDefaultColor(sliderItems, viewPager2));

        View backBtn = view.findViewById(R.id.view_page_back);
        View backBtnIc = view.findViewById(R.id.view_page_back_ic);
        View nextBtn = view.findViewById(R.id.view_page_next);
        View nextBtnIc = view.findViewById(R.id.view_page_next_ic);

        setPageBtnVisibility(currentResult, resultsSize, backBtn, backBtnIc, nextBtn, nextBtnIc);

        TextView textView = view.findViewById(R.id.title);
        textView.setText(title);

        TextView inputSelector = view.findViewById(R.id.thickness_input);

//        if(rootDataKey == "chapas" || rootDataKey == "acero_inoxidable")
            inputSelector.setOnClickListener(this::inputListOnClick);

        View calcBtn = view.findViewById(R.id.calc_btn);
        calcBtn.setOnClickListener(v -> calcBtnOnClick(v, width, length, quantity, selected.get(),
                backBtn, backBtnIc, nextBtn, nextBtnIc));

        backBtn.setOnClickListener(v -> pageBack(backBtn, backBtnIc, nextBtn, nextBtnIc));
        nextBtn.setOnClickListener(v -> pageNext(backBtn, backBtnIc, nextBtn, nextBtnIc));
    }

    private void pageNext(View backBtn, View backBtnIc, View nextBtn, View nextBtnIc) {
        currentResult.set(currentResult.get() + 1);
        viewPager2.setCurrentItem(currentResult.get());
        setPageBtnVisibility(currentResult, resultsSize, backBtn, backBtnIc, nextBtn, nextBtnIc);
    }

    private void pageBack(View backBtn, View backBtnIc, View nextBtn, View nextBtnIc) {
        currentResult.set(currentResult.get() - 1);
        viewPager2.setCurrentItem(currentResult.get());
        setPageBtnVisibility(currentResult, resultsSize, backBtn, backBtnIc, nextBtn, nextBtnIc);
    }

    private void setPageBtnVisibility(AtomicInteger current, AtomicInteger size, View backBtn, View backBtnIc,
                                      View nextBtn, View nextBtnIc) {
        if (current.get() == 0) {
            backBtn.setVisibility(View.GONE);
            backBtnIc.setVisibility(View.GONE);
        } else {
            backBtn.setVisibility(View.VISIBLE);
            backBtnIc.setVisibility(View.VISIBLE);
        }
        if (current.get() == size.get()) {
            nextBtn.setVisibility(View.GONE);
            nextBtnIc.setVisibility(View.GONE);
        } else {
            nextBtn.setVisibility(View.VISIBLE);
            nextBtnIc.setVisibility(View.VISIBLE);
        }
    }

    private void inputListOnClick(View view) {
        actualView = view;
        Intent intent = new Intent(view.getContext(), ListLayout.class);
        String pageTitle = title.toLowerCase(Locale.ROOT)
                .replaceAll(" ", "_");
        String columnNameKey = pageTitle + "_names";
        selectableKey = UtilService.splitAsArray(data.get(columnNameKey))[0];
        String columnName = selectableKey.toLowerCase(Locale.ROOT)
                .replaceAll(" ", "_");
        ArrayList<String> selectableValues =
                new ArrayList<>(UtilService.splitAsList(data.get(pageTitle + "_" + columnName)));
        intent.putStringArrayListExtra(columnName, selectableValues);
        intent.putExtra("list_name", columnName);
        intent.putExtra("show_name", selectableKey);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent results) {
        super.onActivityResult(requestCode, resultCode, results);
        if (results == null)
            return;
        Integer i = (Integer) results.getExtras()
                .get("selectedPosition");
        selected.set(i);
        String key = selectableKey.toLowerCase(Locale.ROOT)
                .replaceAll(" ", "_");
        TextView input = actualView.findViewById(R.id.thickness_input);
        String name = UtilService.splitAsList(data.get(rootDataKey + "_" + key))
                .get(i);
        input.setText(name.replace("//", "/"));
    }

    private void calcBtnOnClick(View view, EditText widthEditText, EditText lengthEditText, EditText quantityEditText,
                                Integer selected, View backBtn, View backBtnIc, View nextBtn, View nextBtnIc) {
        if (isInvalidInput(view, widthEditText, lengthEditText, quantityEditText, selected)) return;
        Double width = Double.valueOf(widthEditText.getText()
                .toString());
        Double length = Double.valueOf(lengthEditText.getText()
                .toString());
        Integer quantity = 1;
        if (quantityEditText != null){
            quantity = Integer.valueOf(quantityEditText.getText()
                    .toString());
        }

        List<String> results = CalculationService.calculate(view.getContext(), width, length,
                quantity, selected, title, data, dataKeys);
        if (results == null)
            return;
        sliderItems.clear();
        for (String result : results) {
            sliderItems.add(new SliderItem(result));
        }
        viewPager2.setAdapter(new SliderAdapter(sliderItems, viewPager2));
        currentResult.set(viewPager2.getCurrentItem());
        resultsSize.set(viewPager2.getAdapter()
                .getItemCount() - 1);
        setPageBtnVisibility(currentResult, resultsSize, backBtn, backBtnIc, nextBtn, nextBtnIc);
    }

    private boolean isInvalidInput(View view, EditText width, EditText length, EditText quantity, Integer selected) {
        if (width.getText()
                .toString()
                .isEmpty()) {
            Toast.makeText(view.getContext(), "Inserte el ancho.", Toast.LENGTH_SHORT)
                    .show();
            return true;
        }
        if (length.getText()
                .toString()
                .isEmpty()) {
            Toast.makeText(view.getContext(), "Inserte el largo.", Toast.LENGTH_SHORT)
                    .show();
            return true;
        }
        if (quantity != null && quantity.getText()
                .toString()
                .isEmpty()) {
            Toast.makeText(view.getContext(), "Inserte la cantidad.", Toast.LENGTH_SHORT)
                    .show();
            return true;
        }
        if (selected == -1) {
            Toast.makeText(view.getContext(), "Elija una chapa.", Toast.LENGTH_SHORT)
                    .show();
            return true;
        }
        return false;
    }
}