(window.webpackJsonp=window.webpackJsonp||[]).push([[25],{448:function(t,e,o){"use strict";o.r(e);var r=o(56),a=Object(r.a)({},(function(){var t=this,e=t.$createElement,o=t._self._c||e;return o("ContentSlotsDistributor",{attrs:{"slot-key":t.$parent.slotKey}},[o("h1",{attrs:{id:"my-first-bot-tutorial"}},[o("a",{staticClass:"header-anchor",attrs:{href:"#my-first-bot-tutorial"}},[t._v("#")]),t._v(" My First Bot tutorial")]),t._v(" "),o("p",[t._v("This tutorial is about getting an introduction to creating your first bot for Robocode Tank Royale.")]),t._v(" "),o("p",[t._v("You might also have a look at the provided sample bots for Robocode for inspiration. You might also use the sample bots\nto provide a template containing all the necessary files to create a bot for a specific programming language and\nplatform.")]),t._v(" "),o("p",[t._v("Note that this tutorial is aimed towards the "),o("RouterLink",{attrs:{to:"/api/apis.html"}},[t._v("APIs")]),t._v(" available for Robocode Tank Royale.")],1),t._v(" "),o("h2",{attrs:{id:"initial-setup"}},[o("a",{staticClass:"header-anchor",attrs:{href:"#initial-setup"}},[t._v("#")]),t._v(" Initial setup")]),t._v(" "),o("p",[t._v("The first part of this tutorial is about the initial setup which is common for all bots that must be "),o("em",[t._v("booted")]),t._v(" by the\ngame regardless of which programming language is used when developing the bot.")]),t._v(" "),o("p",[t._v("I recommend that you read about the "),o("RouterLink",{attrs:{to:"/articles/booter.html"}},[t._v("booter")]),t._v(" first before continuing on this tutorial as the\nfollowing assumes you are somewhat familiar with the file name conventions, and the concept of "),o("em",[t._v("bot directories")]),t._v(",\nand "),o("em",[t._v("root directories")]),t._v(".")],1),t._v(" "),o("h2",{attrs:{id:"prepare-a-root-directory"}},[o("a",{staticClass:"header-anchor",attrs:{href:"#prepare-a-root-directory"}},[t._v("#")]),t._v(" Prepare a root directory")]),t._v(" "),o("p",[t._v("Robocode needs to locate your bot, which must be stored into its own "),o("em",[t._v("bot directory")]),t._v(" under a "),o("em",[t._v("root directory")]),t._v(". The\npurpose of the root directory is to contain one to many bot directories.")]),t._v(" "),o("p",[t._v("So the first step is to prepare a root directory which we name "),o("em",[t._v("bots")]),t._v(". Under Windows, you might create this folder\nunder "),o("code",[t._v("C:\\bots")]),t._v(" or "),o("code",[t._v("%userprofile%\\bots")]),t._v(", and for macOS or Linux you might create a folder under "),o("code",[t._v("~/bots")]),t._v(".")]),t._v(" "),o("p",[t._v("If you use the UI for Robocode, you will need to add this root directory in the Bot Root Configuration so Robocode will\nbe able to locate your bot(s).")]),t._v(" "),o("h2",{attrs:{id:"prepare-a-bot-directory"}},[o("a",{staticClass:"header-anchor",attrs:{href:"#prepare-a-bot-directory"}},[t._v("#")]),t._v(" Prepare a bot directory")]),t._v(" "),o("p",[t._v("Next, you should create a bot directory inside the "),o("em",[t._v("bots")]),t._v(" directory for your first bot, which we name "),o("em",[t._v("MyFirstBot")]),t._v(", so\nit will be located under "),o("code",[t._v("../bots/MyFirstBot")]),t._v(". All your bot files must be put into this folder and share the same file\nname as the bot directory (more info in the "),o("RouterLink",{attrs:{to:"/articles/booter.html"}},[t._v("booter")]),t._v(" article).")],1),t._v(" "),o("h2",{attrs:{id:"create-a-json-file-for-bot-info"}},[o("a",{staticClass:"header-anchor",attrs:{href:"#create-a-json-file-for-bot-info"}},[t._v("#")]),t._v(" Create a JSON file for bot info")]),t._v(" "),o("p",[t._v("A "),o("a",{attrs:{href:"https://fileinfo.com/extension/json",target:"_blank",rel:"noopener noreferrer"}},[t._v("JSON"),o("OutboundLink")],1),t._v(" file is used for providing the game with information about your bot. You\nmust create a MyFirstBot.json file and put this into your bot directory, i.e. into\n"),o("code",[t._v("../bots/MyFirstBot/MyFirstBot.json")]),t._v(".")]),t._v(" "),o("p",[t._v("This is the content of the JSON file, which you can copy and paste into the file:")]),t._v(" "),o("div",{staticClass:"language-json extra-class"},[o("pre",{pre:!0,attrs:{class:"language-json"}},[o("code",[o("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("{")]),t._v("\n  "),o("span",{pre:!0,attrs:{class:"token property"}},[t._v('"name"')]),o("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),o("span",{pre:!0,attrs:{class:"token string"}},[t._v('"My First Bot"')]),o("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n  "),o("span",{pre:!0,attrs:{class:"token property"}},[t._v('"version"')]),o("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),o("span",{pre:!0,attrs:{class:"token string"}},[t._v('"1.0"')]),o("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n  "),o("span",{pre:!0,attrs:{class:"token property"}},[t._v('"gameTypes"')]),o("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),o("span",{pre:!0,attrs:{class:"token string"}},[t._v('"melee, classic, 1v1"')]),o("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n  "),o("span",{pre:!0,attrs:{class:"token property"}},[t._v('"authors"')]),o("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),o("span",{pre:!0,attrs:{class:"token string"}},[t._v('"[Your name]"')]),o("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n  "),o("span",{pre:!0,attrs:{class:"token property"}},[t._v('"description"')]),o("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),o("span",{pre:!0,attrs:{class:"token string"}},[t._v('"My first bot"')]),o("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n  "),o("span",{pre:!0,attrs:{class:"token property"}},[t._v('"homepage"')]),o("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),o("span",{pre:!0,attrs:{class:"token string"}},[t._v('""')]),o("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n  "),o("span",{pre:!0,attrs:{class:"token property"}},[t._v('"countryCodes"')]),o("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),o("span",{pre:!0,attrs:{class:"token string"}},[t._v('"[Your country code, e.g. us]"')]),o("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n  "),o("span",{pre:!0,attrs:{class:"token property"}},[t._v('"platform"')]),o("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),o("span",{pre:!0,attrs:{class:"token string"}},[t._v('"[Programming platform, e.g. Java or .Net]"')]),o("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n  "),o("span",{pre:!0,attrs:{class:"token property"}},[t._v('"programmingLang"')]),o("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),o("span",{pre:!0,attrs:{class:"token string"}},[t._v('"[Programming language, e.g. Java or C#]"')]),t._v("\n"),o("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("}")]),t._v("\n")])])]),o("p",[t._v("Note that the "),o("em",[t._v("authors")]),t._v(" field should contain your full name, nickname, or handle, which identifies you. The "),o("em",[t._v("platform")]),t._v("\nand "),o("em",[t._v("programmingLang")]),t._v(" depends on your choice of programming language and platform. For example, the platform could be\n"),o("em",[t._v("Java 17")]),t._v(" with the programming Language "),o("em",[t._v("Kotlin 1.6.10")]),t._v(" or "),o("em",[t._v("Java 17")]),t._v(", or the platform could be "),o("em",[t._v(".Net 6.0")]),t._v(" with the\nprogramming language "),o("em",[t._v("C# 10.0")]),t._v(" or "),o("em",[t._v("F# 6.0")]),t._v(".")]),t._v(" "),o("p",[t._v("This concludes the common part of the tutorial.")]),t._v(" "),o("h2",{attrs:{id:"select-platform"}},[o("a",{staticClass:"header-anchor",attrs:{href:"#select-platform"}},[t._v("#")]),t._v(" Select platform")]),t._v(" "),o("p",[t._v("The rest of the tutorial is split up based on the available APIs for different platforms:")]),t._v(" "),o("ul",[o("li",[o("RouterLink",{attrs:{to:"/tutorial/dotnet/my-first-bot-for-dotnet.html"}},[t._v(".Net")])],1),t._v(" "),o("li",[o("RouterLink",{attrs:{to:"/tutorial/jvm/my-first-bot-for-jvm.html"}},[t._v("Java / JVM")])],1)])])}),[],!1,null,null,null);e.default=a.exports}}]);