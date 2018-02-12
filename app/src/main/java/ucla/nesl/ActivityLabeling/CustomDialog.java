package ucla.nesl.ActivityLabeling;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by zxxia on 12/21/17.
 */

public class CustomDialog extends AppCompatDialogFragment {

    private EditText mInputEt;
    private ListView mListView;
    private Button mAddBtn;

    private ArrayList<String> mItems;

    CustomListAdapter mItemsAdapter;



    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static CustomDialog newInstance(ArrayList<String> items) {
        CustomDialog frag = new CustomDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putStringArrayList("items", items);
        frag.setArguments(args);
        return frag;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //return super.onCreateDialog(savedInstanceState);

        mItems = getArguments().getStringArrayList("items");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.custom_dialog, null);
        builder.setView(view)
                .setTitle("Customize user defined selections")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        mInputEt = view.findViewById(R.id.customET);
        mAddBtn = view.findViewById(R.id.customAddBtn);
        mAddBtn.setOnClickListener(new View.OnClickListener (){
            @Override
            public void onClick(View v) {
                String content = mInputEt.getText().toString();
                if (!content.isEmpty()) {

                    mItems.add(content);
                    mInputEt.setText("");
                    mItemsAdapter.notifyDataSetChanged();
                }
            }
        });
        mListView = view.findViewById(R.id.dialogLV);
        mItemsAdapter = new CustomListAdapter(getContext(), mItems);
        mListView.setAdapter(mItemsAdapter);
        return builder.create();
    }

}
