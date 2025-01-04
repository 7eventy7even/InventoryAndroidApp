package vkx64.android.scanventory.dialog;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import vkx64.android.scanventory.R;
import vkx64.android.scanventory.utilities.SingleImagePicker;

public class AddGroupDialogFragment extends DialogFragment {

    private AddGroupDialogListener listener;
    private Uri selectedImageUri;
    private ImageView ivImageGroup;

    public interface AddGroupDialogListener {
        //* Listener interface for form submission *//
        void onSubmit(String groupId, String groupName);
    }

    public AddGroupDialogFragment(AddGroupDialogListener listener) {
        //* Constructor to set the listener *//
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for the dialog
        View view = inflater.inflate(R.layout.dialog_add_group, container, false);

        // Initialize views
        EditText etGroupId = view.findViewById(R.id.etGroupId);
        EditText etGroupName = view.findViewById(R.id.etGroupName);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSubmit = view.findViewById(R.id.btnSubmit);
        ivImageGroup = view.findViewById(R.id.ivImageGroup);

        // Cancel button closes the dialog
        btnCancel.setOnClickListener(v -> dismiss());

        // Handle ImageView click to select an image
        ivImageGroup.setOnClickListener(v -> SingleImagePicker.openImageSelector(this));

        // Submit button validates input and sends data
        btnSubmit.setOnClickListener(v -> {

            String groupId = etGroupId.getText().toString().trim();
            String groupName = etGroupName.getText().toString().trim();

            if (groupId.isEmpty() || groupName.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save the selected image to internal storage
            if (selectedImageUri != null) {
                SingleImagePicker.saveImageToInternalStorage(
                        selectedImageUri,
                        "GroupImages",
                        groupId + ".png",
                        requireContext()
                );
            }

            if (listener != null) listener.onSubmit(groupId, groupName);
            dismiss();
        });

        // Set a filter to reject spaces in the Group ID
        etGroupId.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            // Check for spaces and reject them
            for (int i = start; i < end; i++) {
                if (Character.isWhitespace(source.charAt(i))) {
                    Toast.makeText(requireContext(), "Spaces are not allowed in Group ID.", Toast.LENGTH_SHORT).show();
                    return "";
                }
            }
            return null;
        }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle the result from the image picker
        selectedImageUri = SingleImagePicker.handleImagePickerResult(requestCode, resultCode, data, requireContext());
        if (selectedImageUri != null) {
            SingleImagePicker.displayImage(selectedImageUri, ivImageGroup, requireContext());
        }
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
