
package com.idarkwizard.calculatorapp.slider;

        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.TextView;

        import androidx.recyclerview.widget.RecyclerView;
        import androidx.viewpager2.widget.ViewPager2;

        import com.idarkwizard.calculatorapp.R;

        import java.util.List;

public class SliderAdapterDefaultColor extends RecyclerView.Adapter<SliderAdapterDefaultColor.SliderViewHolder> {

    private List<SliderItem> sliderItems;
    private ViewPager2 viewPager2;

    public SliderAdapterDefaultColor (List<SliderItem> sliderItems, ViewPager2 viewPager2) {
        this.sliderItems = sliderItems;
        this.viewPager2 = viewPager2;
    }

    public SliderViewHolder onCreateViewHolder(ViewGroup parent, int rawType) {
        return new SliderViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.slide_item_container_default_color,
                        parent,
                        false
                )
        );
    }

    public void onBindViewHolder(SliderViewHolder holder, int position) {
        holder.setResult(sliderItems.get(position));
    }

    public int getItemCount() {
        return sliderItems.size();
    }

    class SliderViewHolder extends RecyclerView.ViewHolder {

        TextView result;

        public SliderViewHolder(View view) {
            super(view);
            this.result = view.findViewById(R.id.resultSlide);
        }

        public void setResult(SliderItem resultText) {
            this.result.setText((CharSequence) resultText.getSelectedResult());
        }
    }
}
