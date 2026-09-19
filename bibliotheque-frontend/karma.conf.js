// Karma configuration — tolérant aux environnements lents (CI / conteneurs)
module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    // Launcher Headless sans sandbox (obligatoire dans conteneurs/CI) avec délais élargis
    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: ['--no-sandbox', '--disable-dev-shm-usage']
      }
    },
    client: {
      jasmine: {},
      clearContext: false // laisser Jasmine visible dans la console
    },
    jasmineNodeOpts: {
      showColors: true,
      defaultTimeoutInterval: 30000,
      print: function () {} // suppression du bruit de console côté navigateur
    },
    // Clé anti-déconnexion : Chrome Headless est tué par défaut après ~10s de silence
    browserDisconnectTimeout: 120000,
    browserNoActivityTimeout: 240000,
    captureTimeout: 180000,
    restartOnFileChange: true,
    reporters: ['progress'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_WARN,
    autoWatch: true,
    browsers: ['ChromeHeadlessNoSandbox'],
    singleRun: false
  });
};
