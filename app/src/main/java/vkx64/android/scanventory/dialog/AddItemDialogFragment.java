package vkx64.android.scanventory.dialog;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import vkx64.android.scanventory.R;

public class AddItemDialogFragment extends DialogFragment {

    private AddItemDialogListener listener;

    public interface AddItemDialogListener {
        //* Listener interface for form submission *//
        void onSubmit(String itemId, String itemName, String itemCategory, int itemStorage, int itemSelling);
    }

    public AddItemDialogFragment(AddItemDialogListener listener) {
        //* Constructor to set the listener *//
        this.listener = listener;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for the dialog
        View view = inflater.inflate(R.layout.dialog_add_item, container, false);

        // Initialize views
        EditText etItemId = view.findViewById(R.id.etItemId);
        EditText etItemName = view.findViewById(R.id.etItemName);
        EditText etItemCategory = view.findViewById(R.id.etItemCategory);
        EditText etItemStorage = view.findViewById(R.id.etItemStorage);
        EditText etItemSelling = view.findViewById(R.id.etItemSelling);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);

        btnCancel.setOnClickListener(v -> dismiss());

        btnSubmit.setOnClickListener(v -> {
            // Get input values
            String itemId = etItemId.getText().toString().trim();
            String itemName = etItemName.getText().toString().trim();
            String itemCategory = etItemCategory.getText().toString().trim();
            String itemStorage = etItemStorage.getText().toString().trim();
            String itemSelling = etItemSelling.getText().toString().trim();

            int storageValue = Integer.parseInt(itemStorage);
            int sellingValue = Integer.parseInt(itemSelling);

            // Validate inputs
            if (itemId.isEmpty() || itemName.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) listener.onSubmit(itemId, itemName, itemCategory, storageValue, sellingValue);
            dismiss();
        });

        // Set a filter to reject spaces in the Group ID
        etItemId.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            // Check for spaces and reject them
            for (int i = start; i < end; i++) {
                if (Character.isWhitespace(source.charAt(i))) {
                    Toast.makeText(requireContext(), "Spaces are not allowed in Item ID.", Toast.LENGTH_SHORT).show();
                    return "";
                }
            }
            return null;
        }
        });

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //* Set the dialog's background to transparent for rounded corners *//
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        return dialog;
    }

    @Override
    public void onStart() {
        //* Set the dialog's width to 90% of the screen width *//
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // Get screen width
            DisplayMetrics metrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int screenWidth = metrics.widthPixels;

            getDialog().getWindow().setLayout((int) (screenWidth * 0.90), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
