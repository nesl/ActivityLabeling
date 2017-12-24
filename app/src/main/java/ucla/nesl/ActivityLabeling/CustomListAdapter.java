package ucla.nesl.ActivityLabeling;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by zxxia on 12/22/17.
 */

public class CustomListAdapter extends BaseAdapter {
    private ArrayList<String> mItems;
    private LayoutInflater mInflater;

    CustomListAdapter(Context context, ArrayList<String> items) {
        mItems = items;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        final View rowView = mInflater.inflate(R.layout.custom_list_item, parent, false);
        if (mItems.get(position).equalsIgnoreCase("Select a microlocation") ||
                mItems.get(position).equalsIgnoreCase("Select an activity type")){
            rowView.setVisibility(View.GONE);
            return rowView;

        }
        final TextView tv = rowView.findViewById(R.id.customTV);
        tv.setText(mItems.get(position));

        final Button btn =  rowView.findViewById(R.id.removeBtn);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mItems.remove(position);
                notifyDataSetChanged();
                rowView.setVisibility(View.INVISIBLE);
            }
        });
        return rowView;
    }
}