package ucla.nesl.ActivityLabeling.edittextmonitor;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.HashMap;

/**
 * Created by timestring on 2/13/18.
 */

public class EditTextMonitor {

    private HashMap<EditText, Double> editTextValues = new HashMap<>();
    private int errorCnts = 0;

    public void registerEditText(EditText text, double minVal, double maxVal, String errorMessage) {
        editTextValues.put(text, null);
        errorCnts++;
        text.addTextChangedListener(new TextValidator(
                text, minVal, maxVal, errorMessage, this));
    }

    public boolean areAllEditTextValid() {
        return errorCnts == 0;
    }

    public double getEditTextValue(EditText text) {
        if (!editTextValues.containsKey(text)) {
            throw new IllegalStateException("The EditText is not registered");
        }
        Double result = editTextValues.get(text);
        if (result == null) {
            throw new IllegalStateException("The value is not present");
        }
        return result;
    }


    /**
     * Update the value of the EditText. The value is set to be null if the value is invalid.
     * @param text: The EditText
     * @param value: The floating value. Null if the value is not valid.
     */
    private void updateTextValue(EditText text, Double value) {
        if (editTextValues.get(text) == null) {
            errorCnts--;
        }
        editTextValues.put(text, value);
        if (editTextValues.get(text) == null) {
            errorCnts++;
        }
    }


    private class TextValidator implements TextWatcher {

        private EditTextMonitor mMonitor;
        private EditText mEditText;
        private double mMinVal, mMaxVal;
        private String mErrorMessage;

        public TextValidator(EditText editText, double minVal, double maxVal, String errorMessage,
                             EditTextMonitor monitor) {
            mEditText = editText;
            mMonitor = monitor;
            mMinVal = minVal;
            mMaxVal = maxVal;
            mErrorMessage = errorMessage;

            validate();
        }

        @Override
        public void afterTextChanged(Editable editable) {
            validate();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int count, int after) {
        }

        private void validate() {
            try {
                double result = Double.valueOf(mEditText.getText().toString());
                if (mMinVal <= result && result <= mMaxVal) {
                    mMonitor.updateTextValue(mEditText, result);
                    mEditText.setError(null);
                } else {
                    mMonitor.updateTextValue(mEditText, null);
                    mEditText.setError(mErrorMessage);
                }
            } catch (NumberFormatException ex) {
                mMonitor.updateTextValue(mEditText, null);
                mEditText.setError(mErrorMessage);
            }
        }
    };
}
