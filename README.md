Pill Pals 💊 medication tracking and information app, built for for Android 🤖


# Setup
Built with Kotlin, using Material, MPAndroidChart, and Google Vision

https://material.io/develop/android/

https://github.com/PhilJay/MPAndroidChart

https://developers.google.com/vision

# Structure

Each navigation page is a Fragment, placed under pillpals/ui/\<fragment-name\>

These fragments leverage:
* layout intents, placed under pillpals/ui, which inflate layouts in res/layout
* service intents, placed under pillpals/services
* helper classes, placed under pillpals/helpers
* seeds and API data models, placed under pillpals/data
* Realm data models, placed under pillpals/data/models
* our OCR reader, placed under pillpals/ocrreader

Each layout, either for a fragment or for a layout intent, is placed under res/layout

# Screenshots

<img alt="Screenshot src="/screenshots/dashboard_page.png" width="250px"></img>
<img alt="Screenshot src="/screenshots/search_page.png" width="250px"></img>
<img alt="Screenshot src="/screenshots/stats_page.png" width="250px"></img>
<img alt="Screenshot src="/screenshots/medications_page.png" width="250px"></img>
<img alt="Screenshot src="/screenshots/medication_info.png" width="250px"></img>
<img alt="Screenshot src="/screenshots/medication_edit.png" width="250px"></img>
<img alt="Screenshot src="/screenshots/quiz_question.png" width="250px"></img>
