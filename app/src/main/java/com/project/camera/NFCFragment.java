package com.project.camera;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class NFCFragment extends BottomSheetDialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
            ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        View v = inflater.inflate(R.layout.fragment_nfc,
                container, false);

        Button readButton = v.findViewById(R.id.read);
        Button writeButton = v.findViewById(R.id.write);

        readButton.setOnClickListener(v12 -> {
            Intent intent = new Intent(requireActivity().getApplicationContext(), NFC.class);
            intent.putExtra("Mode", 0);
            startActivity(intent);
        });

        writeButton.setOnClickListener(v1 -> {
            Intent intent = new Intent(requireActivity().getApplicationContext(), ContactChooser.class);
            startActivity(intent);
        });
        return v;
    }
}