package com.github.dedis.student20_pop.detail.fragments.event.creation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.dedis.student20_pop.databinding.FragmentCreateRollCallEventBinding;
import com.github.dedis.student20_pop.detail.LaoDetailActivity;
import com.github.dedis.student20_pop.detail.LaoDetailViewModel;

/** Fragment that shows up when user wants to create a Roll-Call Event */
public final class RollCallEventCreationFragment extends AbstractEventCreationFragment {

    public static final String TAG = RollCallEventCreationFragment.class.getSimpleName();


    private LaoDetailViewModel mLaoDetailViewModel;
    private FragmentCreateRollCallEventBinding mFragmentCreateRollCallEventBinding;

    private final TextWatcher confirmTextWatcher =
            new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //nothing to do
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String meetingTitle = mFragmentCreateRollCallEventBinding.rollCallTitleText.getText().toString().trim();
                    boolean areFieldsFilled =
                            !meetingTitle.isEmpty() && !getStartDate().isEmpty() && !getStartTime().isEmpty();
                    mFragmentCreateRollCallEventBinding.rollCallOpen.setEnabled(areFieldsFilled);
                    mFragmentCreateRollCallEventBinding.rollCallConfirm.setEnabled(areFieldsFilled);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    //nothing to do
                }
            };

    public static RollCallEventCreationFragment newInstance() {
        return new RollCallEventCreationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        mFragmentCreateRollCallEventBinding =
                FragmentCreateRollCallEventBinding.inflate(inflater, container, false);

        mLaoDetailViewModel = LaoDetailActivity.obtainViewModel(getActivity());

        setDateAndTimeView(mFragmentCreateRollCallEventBinding.getRoot(), this, getFragmentManager());
        addDateAndTimeListener(confirmTextWatcher);
        mFragmentCreateRollCallEventBinding.rollCallTitleText.addTextChangedListener(confirmTextWatcher);

        mFragmentCreateRollCallEventBinding.setLifecycleOwner(getActivity());


        return mFragmentCreateRollCallEventBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupConfirmButton();
        setupOpenButton();
        setupCancelButton();

        mLaoDetailViewModel
                .getCreatedRollCallEvent()
                .observe(
                        this,
                        booleanEvent -> {
                            Boolean action = booleanEvent.getContentIfNotHandled();
                            if (action != null) {
                                mLaoDetailViewModel.openLaoDetail();
                            }
                        });
    }

    void setupConfirmButton(){
        mFragmentCreateRollCallEventBinding.rollCallConfirm.setOnClickListener(
                v -> {
                    computeTimesInSeconds();

                    String title = mFragmentCreateRollCallEventBinding.rollCallTitleText.getText().toString();
                    String description = mFragmentCreateRollCallEventBinding.rollCallEventDescriptionText.getText().toString();
                    mLaoDetailViewModel.createNewRollCall(title, description, startTimeInSeconds, endTimeInSeconds, false);
                });
    }
    void setupOpenButton(){
        mFragmentCreateRollCallEventBinding.rollCallOpen.setOnClickListener(
                v -> {
                    computeTimesInSeconds();

                    String title = mFragmentCreateRollCallEventBinding.rollCallTitleText.getText().toString();
                    String description = mFragmentCreateRollCallEventBinding.rollCallEventDescriptionText.getText().toString();
                    mLaoDetailViewModel.createNewRollCall(title, description, startTimeInSeconds, endTimeInSeconds, true);
                });
    }
    void setupCancelButton(){
        mFragmentCreateRollCallEventBinding.rollCallCancel.setOnClickListener(
            v ->
                mLaoDetailViewModel.openLaoDetail()
        );
    }
}