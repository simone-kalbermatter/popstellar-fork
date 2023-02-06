package com.github.dedis.popstellar.ui.detail.token;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.dedis.popstellar.R;
import com.github.dedis.popstellar.databinding.TokenListFragmentBinding;
import com.github.dedis.popstellar.model.objects.RollCall;
import com.github.dedis.popstellar.repository.RollCallRepository;
import com.github.dedis.popstellar.ui.detail.event.rollcall.RollCallViewModel;
import com.github.dedis.popstellar.ui.lao.LaoActivity;
import com.github.dedis.popstellar.ui.lao.LaoViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.android.schedulers.AndroidSchedulers;

@AndroidEntryPoint
public class TokenListFragment extends Fragment {

  public static final String TAG = TokenListFragment.class.getSimpleName();

  private TokenListFragmentBinding binding;
  private LaoViewModel viewModel;
  private RollCallViewModel rollCallViewModel;
  private TokenListAdapter tokensAdapter;

  @Inject RollCallRepository rollCallRepo;

  public static TokenListFragment newInstance() {
    return new TokenListFragment();
  }

  public TokenListFragment() {
    // Required empty public constructor
  }

  @Override
  public void onResume() {
    super.onResume();
    viewModel.setPageTitle(R.string.tokens);
    viewModel.setIsTab(true);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {

    binding = TokenListFragmentBinding.inflate(inflater, container, false);
    viewModel = LaoActivity.obtainViewModel(requireActivity());
    rollCallViewModel =
        LaoActivity.obtainRollCallViewModel(requireActivity(), viewModel.getLaoId());

    tokensAdapter = new TokenListAdapter(requireActivity());
    binding.tokensRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    binding.tokensRecyclerView.setAdapter(tokensAdapter);

    subscribeToAttendedRollCalls();

    return binding.getRoot();
  }

  private void subscribeToAttendedRollCalls() {
    viewModel.addDisposable(
        rollCallViewModel
            .getAttendedRollCalls()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                attendedRollCalls -> {
                  RollCall lastRollCall = rollCallRepo.getLastClosedRollCall(viewModel.getLaoId());

                  if (attendedRollCalls.contains(lastRollCall)) {
                    // We attended the last roll call
                    TextView validRcTitle =
                        binding.validTokenLayout.findViewById(R.id.token_layout_rc_title);
                    validRcTitle.setText(lastRollCall.getName());
                    binding.validTokenLayout.setVisibility(View.VISIBLE);
                    binding.validTokenCard.setOnClickListener(
                        v ->
                            LaoActivity.setCurrentFragment(
                                requireActivity().getSupportFragmentManager(),
                                R.id.fragment_token,
                                () -> TokenFragment.newInstance(lastRollCall.getPersistentId())));
                  } else {
                    binding.validTokenLayout.setVisibility(View.GONE);
                  }

                  // This handle the previous tokens list
                  // First we remove the last roll call from the list of attended roll calls
                  List<RollCall> previousRollCalls = new ArrayList<>(attendedRollCalls);
                  previousRollCalls.remove(lastRollCall);
                  if (previousRollCalls.isEmpty()) {
                    binding.previousTokenLayout.setVisibility(View.GONE);
                  } else {
                    binding.previousTokenLayout.setVisibility(View.VISIBLE);
                    tokensAdapter.replaceList(previousRollCalls);
                  }
                  binding.emptyTokenLayout.setVisibility(View.GONE);
                },
                error -> {
                  // In case of error, such as when no closed rc exists, we display an explanatory
                  // message to the user
                  binding.emptyTokenLayout.setVisibility(View.VISIBLE);
                  binding.validTokenLayout.setVisibility(View.GONE);
                  binding.previousTokenLayout.setVisibility(View.GONE);
                }));
  }
}
