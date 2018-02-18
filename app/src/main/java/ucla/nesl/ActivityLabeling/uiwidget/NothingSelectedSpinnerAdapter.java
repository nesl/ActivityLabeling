package ucla.nesl.ActivityLabeling.uiwidget;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import ucla.nesl.ActivityLabeling.R;

/**
 * This was originally developed by `aaronvargas` on StackOverflow and is modified by timestring
 * to fit into the need of this project. Please see the original post at:
 * https://stackoverflow.com/a/12221309/4713342
 *
 * Decorator Adapter to allow a Spinner to show a 'Nothing Selected...' initially displayed
 * instead of the first choice in the Adapter.
 *
 * Note when registering an OnItemSelectedListener, use the field id to capture the index of item
 * selected corresponding to the arraylist.
 *
 */
public class NothingSelectedSpinnerAdapter implements SpinnerAdapter, ListAdapter {

    protected static final int EXTRA = 1;
    protected static final int DEFAULT_NOTHING_SELECTED_DROPDOWN_LAYOUT_ID = -1;

    protected SpinnerAdapter adapter;
    protected Context context;
    protected int nothingSelectedLayout;
    protected String nothingSelectedTextMessage = "";
    protected int nothingSelectedDropdownLayout;
    protected LayoutInflater layoutInflater;


    /**
     * Use this constructor to Define your 'Select One...' layout as the first
     * row in the returned choices.
     * If you do this, you probably don't want a prompt on your spinner or it'll
     * have two 'Select' rows.
     * @param spinnerAdapter wrapped Adapter. Should probably return false for isEnabled(0)
     * @param nothingSelectedMessage the message to be appeared when nothing is selected. Served as
     *                               a hint for user to select items
     * @param context
     */
    public NothingSelectedSpinnerAdapter(SpinnerAdapter spinnerAdapter,
                                         String nothingSelectedMessage, Context context) {
        this.adapter = spinnerAdapter;
        this.context = context;
        this.nothingSelectedLayout = R.layout.spinner_row_nothing_selected;
        this.nothingSelectedTextMessage = nothingSelectedMessage;
        this.nothingSelectedDropdownLayout = DEFAULT_NOTHING_SELECTED_DROPDOWN_LAYOUT_ID;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        // This provides the View for the Selected Item in the Spinner, not
        // the dropdown (unless dropdownView is not set).
        if (position == 0) {
            return getNothingSelectedView(parent);
        }
        return adapter.getView(position - EXTRA, null, parent); // Could re-use
        // the convertView if possible.
    }

    /**
     * View to show in Spinner with Nothing Selected
     * Override this to do something dynamic... e.g. "37 Options Found"
     * @param parent
     * @return
     */
    protected View getNothingSelectedView(ViewGroup parent) {
        View layout = layoutInflater.inflate(nothingSelectedLayout, parent, false);
        TextView textView = layout.findViewById(R.id.text);
        textView.setText(nothingSelectedTextMessage);
        return layout;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        // Android BUG! http://code.google.com/p/android/issues/detail?id=17128 -
        // Spinner does not support multiple view types
        if (position == 0) {
            return nothingSelectedDropdownLayout == -1 ?
                    new View(context) :
                    getNothingSelectedDropdownView(parent);
        }

        // Could re-use the convertView if possible, use setTag...
        return adapter.getDropDownView(position - EXTRA, null, parent);
    }

    /**
     * Override this to do something dynamic... For example, "Pick your favorite
     * of these 37".
     * @param parent
     * @return
     */
    protected View getNothingSelectedDropdownView(ViewGroup parent) {
        return layoutInflater.inflate(nothingSelectedDropdownLayout, parent, false);
    }

    @Override
    public int getCount() {
        int count = adapter.getCount();
        return count == 0 ? 0 : count + EXTRA;
    }

    @Override
    public Object getItem(int position) {
        return position == 0 ? null : adapter.getItem(position - EXTRA);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position >= EXTRA ? adapter.getItemId(position - EXTRA) : position - EXTRA;
    }

    @Override
    public boolean hasStableIds() {
        return adapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return adapter.isEmpty();
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        adapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        adapter.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0; // Don't allow the 'nothing selected' item to be picked.
    }

}