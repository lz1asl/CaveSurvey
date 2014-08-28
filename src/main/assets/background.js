chrome.app.runtime.onLaunched.addListener(function() {
    var screenWidth = screen.availWidth;
    var screenHeight = screen.availHeight;
    var width = screenWidth/2;
    var height = screenHeight/2;

    chrome.app.window.create('index.html',
            {width: width, height: height,
                left: 0,
                top: 0});
});