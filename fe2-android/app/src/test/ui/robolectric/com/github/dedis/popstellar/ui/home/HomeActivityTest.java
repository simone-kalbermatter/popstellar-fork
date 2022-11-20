package com.github.dedis.popstellar.ui.home;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;

import dagger.hilt.android.testing.HiltAndroidRule;
import dagger.hilt.android.testing.HiltAndroidTest;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.github.dedis.popstellar.testutils.UITestUtils.dialogNeutralButton;
import static com.github.dedis.popstellar.testutils.UITestUtils.dialogPositiveButton;
import static com.github.dedis.popstellar.testutils.pages.home.HomePageObject.createButton;
import static com.github.dedis.popstellar.testutils.pages.home.HomePageObject.fragmentContainer;
import static com.github.dedis.popstellar.testutils.pages.home.LaoCreatePageObject.createFragmentId;
import static com.github.dedis.popstellar.testutils.pages.home.WalletPageObject.confirmButton;

@HiltAndroidTest
@RunWith(AndroidJUnit4.class)
public class HomeActivityTest {

  //  Hilt rule
  private final HiltAndroidRule hiltAndroidRule = new HiltAndroidRule(this);

  // Activity scenario rule that starts the activity.
  public ActivityScenarioRule<HomeActivity> activityScenarioRule =
      new ActivityScenarioRule<>(HomeActivity.class);

  @Rule
  public final RuleChain rule =
      RuleChain.outerRule(MockitoJUnit.testRule(this))
          .around(hiltAndroidRule)
          .around(activityScenarioRule);

  @Test
  public void createButtonBringsToCreateScreen() {
    initializeWallet(activityScenarioRule);
    createButton().perform(click());
    fragmentContainer().check(matches(withChild(withId(createFragmentId()))));
  }

  public static void initializeWallet(
      ActivityScenarioRule<HomeActivity> activityActivityScenarioRule) {
    activityActivityScenarioRule
        .getScenario()
        .onActivity(
            activity -> {
              HomeViewModel viewModel = HomeActivity.obtainViewModel(activity);
              if (!viewModel.isWalletSetUp()) {
                dialogNeutralButton().performClick();
                confirmButton().perform(click());
                dialogPositiveButton().performClick();
              }
            });
  }
}
