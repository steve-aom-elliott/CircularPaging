package selliott.circularpaging.adpaters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import selliott.circularpaging.R;

public class CircularRecyclerAdapter extends RecyclerView.Adapter<CircularRecyclerAdapter.CircularRecyclerViewHolder> {
    @Override
    public CircularRecyclerViewHolder onCreateViewHolder(final ViewGroup parent, final int position) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_page, parent, false);
        return new CircularRecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final CircularRecyclerViewHolder circularRecyclerViewHolder, final int position) {
        circularRecyclerViewHolder.setPosition(position);
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    public class CircularRecyclerViewHolder extends RecyclerView.ViewHolder {
        private final TextView text;

        public CircularRecyclerViewHolder(final View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.text);
        }
        public void setPosition(final int position) {
            text.setText("hi " + position);
        }
    }
}
