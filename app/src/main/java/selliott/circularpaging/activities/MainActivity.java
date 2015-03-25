package selliott.circularpaging.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Arrays;

import selliott.circularpaging.R;
import selliott.circularpaging.RotatingPagerAdapter;
import selliott.circularpaging.views.CircularViewPager;


public class MainActivity extends ActionBarActivity {

    private RecyclerView recyclerView;
    private CircularViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pager = (CircularViewPager) findViewById(R.id.pager);
        pager.setAdapter(new RotatingPagerAdapter<>(Arrays.asList(generateViewHolder(0), generateViewHolder(1), generateViewHolder(2), generateViewHolder(3), generateViewHolder(4))));
        pager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        pager.setCurrentItemNoRotate(1, true);
//        recyclerView = (RecyclerView) findViewById(R.id.recycler);
//        recyclerView.setLayoutManager(new SnappingLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        recyclerView.setAdapter(new CircularRecyclerAdapter());
//        recyclerView.setHasFixedSize(true);
    }

    private View generateView(final int index) {
        final View view = LayoutInflater.from(pager.getContext()).inflate(R.layout.item_page, pager, false);
        ((TextView)view.findViewById(R.id.text)).setText("" + index);
        return view;
    }

    private RotatingPagerAdapter.ViewHolder generateViewHolder(final int index) {
        final View v = generateView(index);
        return new RotatingPagerAdapter.ViewHolder() {
            @Override
            public View getView() {
                return v;
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
