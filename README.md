
# Keyboard GPT
*Last updated on 12/08/2025*

An **LSPosed Module** that lets you integrate Generative AI like ChatGPT in your favorite keyboard.

- [x] Tested up to Android 15, may work in later versions.
- [x] Works with Rooted and Unrooted devices.
- [x] Works in all apps.
- [x] Works with all keyboards.

<details>  
  <summary>Demo Video : Normal Prompt</summary>  



https://github.com/user-attachments/assets/bc054498-7aa6-4834-bf54-41d5e2b785b6



</details>  

<details>  
  <summary>Demo Video : Custom Prompts</summary>  



https://github.com/user-attachments/assets/ae3a4aff-3744-4b16-a3eb-18da2b5c1f3c



</details>  

<details>  
  <summary>Demo Video : Text Formatting</summary>  


https://github.com/user-attachments/assets/27043163-9920-48e3-bb02-0f68803d0be7



</details>  


<details>  
  <summary>Demo Video : Custom Patterns</summary>  


https://github.com/user-attachments/assets/80e9abab-7a4b-404e-9777-2d22436ce5e2



</details>  

<details>  
  <summary>Demo Video : Web Search</summary>  


https://github.com/user-attachments/assets/4cbf9088-0fb2-45d1-9a89-c0c72c1aff6e


</details>  

<p align="center">  
  <img src="demo/icon_border.png" alt="Icon" style="border: 10px solid black;"/>  
</p>  

## Tested Keyboards

To this date, all tested keyboards are fully compatible with KeyboardGPT
- [Google Gboard](https://play.google.com/store/apps/details?id=com.google.android.inputmethod.latin)
- [Microsoft Swiftkey](https://play.google.com/store/apps/details?id=com.touchtype.swiftkey)
- [Yandex Keyboard](ru.yandex.androidkeyboard)
- [Simple Keyboard](https://play.google.com/store/apps/details?id=rkr.simplekeyboard.inputmethod)
- [Futo Keyboard](https://play.google.com/store/apps/details?id=org.futo.inputmethod.latin.playstore)

PS: If a keyboard is not in this list, it means that it has not been tested yet.
However, even if your keyboard is not there, there is a very high chance that it's also supported by KeyboardGPT, as I haven't found an unsupported keyboard among those I have tested.

## Features

- AI chat completions (supports normal and custom prompts)
- Text Formatting (bold, italic, ...)
- Web Search

## Install Guide

#### Root
1. Install module apk from [releases](https://github.com/Mino260806/KeyboardGPT/releases/)
2. Enable module in LSPosed and select your favorite keyboard
3. Force close the keyboard from settings, or if you don't know how, restart you phone

#### No Root
1. Install module apk from [releases](https://github.com/Mino260806/KeyboardGPT/releases/)
2. Patch your favorite keyboard apk in LSPatch Manager and follow the instructions

<details>  
  <summary>Video : Using LSPatch (No Root)</summary>  


https://github.com/user-attachments/assets/ebacb5e4-08fb-45ff-8dcd-4d9b8f9232a0



</details>

Note: With *No Root* method, you cannot patch a system app (Mostly Gboard or Samsung Keyboard, depending on your system).

## Usage Guide

- `*#settings#*` open module settings
- `$<prompt>$` submit a normal prompt
- `$$` configure API provider (API key, model, ...)
- `%<prefix> <prompt>%` submit a custom prompt
- `%%` configure custom promtps
- `%s <text>%` do a web search
- `|<text>|` transform your text into *italic* form
- `@<text>@` transform your text into **bold** form
- `~<text>~` transform your text into ~~crossout~~ form
- `_<text>_` transform your text into <u>underline</u> form

See Demo Videos above

**<u>Bonus Tip</u>**:  Providers that offer free API access (as of August 2025)
+ Google (Gemini). [Grab a key](https://aistudio.google.com/app/apikey)
+ Groq. [Grab a key](https://console.groq.com/keys)
+ OpenRouter. [Grab a key](https://openrouter.ai/settings/keys)

## Supported Generative AI APIs

- Gemini
- ChatGPT
- Groq
- OpenRouter
- Claude

More suggestions are welcome !

## Links
[XDA Link](https://xdaforums.com/t/mod-xposed-integrate-generative-ai-like-chatgpt-in-keyboard.4683421/)

[Telegram Discussion](https://t.me/keyboard_gpt)
