function fn() {
  var env = karate.env; // get system property 'karate.env'
  karate.log('karate.env system property was:', env);
  if (!env) {
    env = 'go_client'
  }
  karate.log('karate.env system property is set to', env);
  var config = {
    env: env,
    host: 'hostname',    //Host of the server
    port: 0o000,  //Port
    path: 'URI path', //URI Path
    wsURL: 'ws/url/port/path', //Server url
    timeout: 5000, //Timeout for websocket response
    args: [],
  }
  if (env === 'go_client') {
    // customize
    config.host = '127.0.0.1';
    config.port = 9000;
    config.path = 'client';
    config.wsURL = `ws://${config.host}:${config.port}/${config.path}`;
  } else if (env === 'go_server') {
    // customize
    config.host = '127.0.0.1';
    config.port = 9001;
    config.path = 'server';
    config.wsURL = `ws://${config.host}:${config.port}/${config.path}`;
  } else if (env === 'scala_client') {
    // customize
    config.host = '127.0.0.1';
    config.port = 8000;
    config.path = 'client';
    config.wsURL = `ws://${config.host}:${config.port}/${config.path}`;
  } else if (env === 'scala_server') {
    // customize
    config.host = '127.0.0.1';
    config.port = 8000; // Scala back-end does not have a specific server port
    config.path = 'server';
    config.wsURL = `ws://${config.host}:${config.port}/${config.path}`;
  } else {
    config.port = 9005;
    config.timeout = 1000;

    if (env === 'web') {
      config.max_input_retry = 10;
    } else if (env === 'android') {
      const android = {};
      android["desiredConfig"] = {
        "app" : "../../fe2-android/app/build/outputs/apk/debug/app-debug.apk",
        "newCommandTimeout" : 1000,
        "platformVersion" : "9.0",
        "platformName" : "Android",
        "connectHardwareKeyboard" : true,
        "deviceName" : "emulator-5554",
        "avd" : "Pixel_4_API_30",
        "automationName" : "UiAutomator2",
        "autoGrantPermissions" : true
      }
      config["android"] = android
    }
  }

  return config;
}
