# Mostafa MultiWidget

[فارسی](#فارسی) · [English](#english)

---

<a name="فارسی"></a>
## فارسی

ویجت و برنامه‌ی همه‌کاره برای **اندروید**: آی‌پی و کشور، سرعت لحظه‌ای اینترنت، دمای هوا، تاریخ و مناسبت‌ها، وقت اذان و نرخ دلار، همه در یک‌جا.

نسخه‌ی ویندوز در ریپازیتوری جدا: https://github.com/coxvpn/windows-Multiwidget

ساخته‌شده توسط مصطفی ماندگاری.


### اندروید

نیازمند اندروید ۸ یا بالاتر.

**ویژگی‌ها**
- آی‌پی عمومی، پرچم، نام کشور، شهر و ارائه‌دهنده‌ی اینترنت. به‌جای آی‌پی می‌توانید ساعت را نشان دهید.
- سه طرح ویجت (طرح اصلی و دو طرح رنگی) با چند پس‌زمینه.
- دما، کمینه و بیشینه و شاخص آلودگی هوا (AQI). شهر را با جستجو یا GPS انتخاب می‌کنید.
- تاریخ شمسی و میلادی، تعطیلی‌ها (🔴) و مناسبت‌ها (🌙).
- زمان مانده تا اذان (وقت شیعه، بدون نیاز به اینترنت) و گفتن «الله اکبر» سر وقت اذان.
- نرخ دلار، مصرف اینترنت امروز (وای‌فای و سیم‌کارت جدا) و نمودار ۷ روز اخیر.
- اعلان زنده در نوار بالا: سرعت لحظه‌ای، دما، تاریخ، اذان، مصرف و نرخ دلار؛ هر بخش قابل روشن و خاموش شدن است.
- بیپ ساعتی (تا ۵ ساعت) با ۲۰ آهنگ ملایم یا آهنگ دلخواه از گوشی.
- منوی برنامه فارسی یا انگلیسی، و دکمه‌ی بررسی به‌روزرسانی.

**راهنمای اولین اجرا**
1. فایل نصب را نصب کنید (اگر گوشی پرسید، اجازه‌ی «نصب از این منبع» را بدهید).
2. برنامه را باز کنید و به پرسش‌ها پاسخ دهید: اجازه‌ی اعلان، دسترسی به آمار مصرف اینترنت، و موقعیت (برای پیدا کردن شهر با GPS).
3. پرسش «افزودن ویجت به صفحه‌ی اصلی» را تأیید کنید. بعداً هم از پایین صفحه‌ی اول برنامه می‌توانید ویجت را اضافه کنید.

**راهنمای منوی برنامه (☰)**
- **چه چیزهایی نشان داده شود:** طرح و پس‌زمینه‌ی ویجت، دما، آلودگی هوا، اذان، تاریخ، دلار، مصرف اینترنت، و اینکه خط آی‌پی «آی‌پی» یا «ساعت» نشان دهد.
- **شهر:** جستجوی نام شهر یا گرفتن از GPS؛ دما، آلودگی و اذان از روی آن حساب می‌شوند.
- **نوار اعلان:** روشن یا خاموش کردن اعلان و انتخاب اجزای آن. ترتیب: سرعت، دما، تاریخ، اذان، مصرف، دلار.
- **بیپ ساعتی و صدای اذان:** زمان‌ها، روزها و صدا.
- **دسترسی‌ها:** آمار مصرف، اجرای بدون محدودیت باتری، آلارم دقیق.
- **درباره:** زبان، بررسی به‌روزرسانی و لینک کد.


### امضای برنامه (کلید خصوصی)

ساخت برنامه با یک کلید خصوصی امضا می‌شود تا نسخه‌های تازه روی قبلی نصب شوند و گوگل پلی‌پروتکت اعتماد بیشتری کند. کلید در کد نیست؛ باید ۴ مقدار را در **Settings ← Secrets and variables ← Actions ← New repository secret** بسازید: `KEYSTORE_BASE64`، `KEYSTORE_PASSWORD`، `KEY_ALIAS` و `KEY_PASSWORD`. بدون آن‌ها ساخت با پیام خطای روشن متوقف می‌شود. فایل کلید (`.jks`) را هرگز در ریپازیتوری نگذارید و یک نسخه‌ی پشتیبان از آن نگه دارید؛ با گم شدنش نمی‌توان نسخه‌ی تازه روی نصب‌های قبلی داد.

---

<a name="english"></a>
## English

An all-in-one widget and app for **Android**: IP address and country, live internet speed, weather, date and occasions, prayer times and the dollar rate, all in one place.

The Windows version has its own repository: https://github.com/coxvpn/windows-Multiwidget

Made by Mostafa Mandegari.


### Android

Requires Android 8 or newer.

**Features**
- Public IP, flag, country, city and provider. You can show the clock instead of the IP.
- Three widget layouts (a main one and two colourful ones) with several backgrounds.
- Temperature, daily high/low and air quality (AQI). Choose the city by search or GPS.
- Persian and Gregorian dates, holidays (🔴) and occasions (🌙).
- Time left to the next prayer call (Shia times, works offline), and the phone says "Allahu Akbar" at prayer time.
- Dollar rate, today's data usage (Wi-Fi and SIM separately) and a 7-day chart.
- A live notification: speed, temperature, date, prayer, usage and dollar rate; every part can be switched on or off.
- Hourly beep (up to 5 times) with 20 soft melodies or your own sound.
- Persian or English app menu, and a "check for updates" button.

**First run**
1. Install the file (if asked, allow "install from this source").
2. Open the app and answer the questions: notification permission, data-usage access and location (the city is found by GPS).
3. Confirm "add the widget to the home screen". You can add it later from the bottom of the app's first page.

**App menu (☰)**
- **What to show:** widget layout and background, temperature, air quality, prayer, date, dollar, data usage, and whether the IP line shows the IP or the clock.
- **City:** search by name or use GPS; temperature, air quality and prayer times depend on it.
- **Notification:** switch it on or off and choose its parts. Order: speed, temperature, date, prayer, usage, dollar.
- **Hourly beep and prayer sound:** times, days and sound.
- **Permissions:** usage access, no battery limits, exact alarms.
- **About:** language, check for updates and the source link.

**Build from source:** run the **Build Android app** workflow in the Actions tab. The version is read from `android/app/build.gradle.kts` (`versionName`) and a Release with `Mostafa-MultiWidget-vX.Y.Z.apk` is published.

### App signing (private key)

The build is signed with a private key, so new versions install over the old ones and Google Play Protect trusts the app more. The key is not in the code: create 4 values in **Settings > Secrets and variables > Actions > New repository secret**: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD`. Without them the build stops with a clear error. Never put the key file (`.jks`) in the repository, and keep a backup of it: if it is lost, a new version can no longer be installed over the old installs.

---

## License and thanks

The code is open source. Thanks to B3H1Z for the original project.
