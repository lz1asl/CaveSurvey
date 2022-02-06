# Unit tests

`./gradlew test`

# Espresso tests

UI tests that require running emulator.
`./gradlew connectedCheck`

See https://stackoverflow.com/a/44005848 :

```
Espresso doesn't work well with animations due to the visual state delays they introduce. You need to disable animations on your device. Firstly, enable developer options:
 - Open the Settings app.
 - Scroll to the bottom and select About phone.
 - Scroll to the bottom and tap Build number 7 times.
 - Return to the previous screen to find Developer options near the bottom.
Access Developer Options from Settings app, and under the Drawing section, switch all of the following options to Animation Off:
 - Window animation scale
 - Transition animation scale
 - Animator duration scale
```