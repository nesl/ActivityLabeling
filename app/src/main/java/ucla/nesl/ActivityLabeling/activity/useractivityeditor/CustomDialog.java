package ucla.nesl.ActivityLabeling.activity.useractivityeditor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import ucla.nesl.ActivityLabeling.R;

/**
 * Created by zxxia on 12/21/17.
 */

public class CustomDialog extends AppCompatDialogFragment {

    private static final String KEY_ITEMS = "items";

    private CustomListAdapter mItemsAdapter;


    public static CustomDialog newInstance(ArrayList<String> items) {
        CustomDialog frag = new CustomDialog();

        // Pass the parameters into a bundle
        Bundle args = new Bundle();
        args.putStringArrayList(KEY_ITEMS, items);
        frag.setArguments(args);
        return frag;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ArrayList<String> items = getArguments().getStringArrayList(KEY_ITEMS);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.custom_dialog, null);
        builder.setView(view)
                .setTitle("Customize your labels")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        // Section of add a new label
        final EditText inputEt = view.findViewById(R.id.customET);

        final Button addBtn = view.findViewById(R.id.customAddBtn);
        addBtn.setOnClickListener(new View.OnClickListener (){
            @Override
            public void onClick(View v) {
                String content = inputEt.getText().toString();
                if (!content.isEmpty()) {
                    items.add(content);
                    inputEt.setText("");
                    mItemsAdapter.notifyDataSetChanged();
                }
            }
        });

        // Section of existing labels
        final ListView listView = view.findViewById(R.id.dialogLV);
        mItemsAdapter = new CustomListAdapter(getContext(), items);
        listView.setAdapter(mItemsAdapter);

        return builder.create();
    }

}
