package fe;

import com.intuit.karate.junit5.Karate;

public class FrontEndTest {

//  @Karate.Test
//  Karate testCreateLAO() {
//    return Karate.run("classpath:fe/LAO/create_lao.feature");
//  }
//
//  @Karate.Test
//  Karate testCreateRC() {
//    return Karate.run("classpath:fe/RollCall/rollCallCreation.feature");
//  }
//
//  @Karate.Test
//  Karate testOpenRC() {
//    return Karate.run("classpath:fe/RollCall/rollCallOpen.feature");
//  }

//  @Karate.Test
//  Karate testCloseRC() {
//    return Karate.run("classpath:fe/RollCall/rollCallClose.feature");
//  }

  @Karate.Test
  Karate testReopenRC() {
    return Karate.run("classpath:fe/RollCall/rollCallReopen.feature");
  }
}
