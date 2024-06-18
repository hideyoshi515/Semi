let currentLanguage = 'default';
let Keyboard = window.SimpleKeyboard.default;
let focusedInput = null;
let korean = [];

document.addEventListener("DOMContentLoaded", function () {
    focusedInput = document.querySelector(".input");

    let keyboard = new Keyboard({
        onChange: input => onChange(input), onKeyPress: button => onKeyPress(button), layout: {
            'default': ['` 1 2 3 4 5 6 7 8 9 0 - = {bksp}', '{tab} q w e r t y u i o p [ ] \\', '{lock} a s d f g h j k l ; \' {enter}', '{shift} z x c v b n m , . / {lang}', '{space}'],
            'shift': ['~ ! @ # $ % ^ & * ( ) _ + {bksp}', '{tab} Q W E R T Y U I O P { } |', '{lock} A S D F G H J K L : " {enter}', '{shift} Z X C V B N M < > ? {lang}', '{space}'],
            'ko': ['` 1 2 3 4 5 6 7 8 9 0 - = {bksp}', '{tab} ㅂ ㅈ ㄷ ㄱ ㅅ ㅛ ㅕ ㅑ ㅐ ㅔ [ ] \\', '{lock} ㅁ ㄴ ㅇ ㄹ ㅎ ㅗ ㅓ ㅏ ㅣ ; \' {enter}', '{shift} ㅋ ㅌ ㅊ ㅍ ㅠ ㅜ ㅡ , . / {lang}', '{space}'],
            'koShift': ['~ ! @ # $ % ^ & * ( ) _ + {bksp}', '{tab} ㅃ ㅉ ㄸ ㄲ ㅆ ㅛ ㅕ ㅑ ㅒ ㅖ { } |', '{lock} ㅁ ㄴ ㅇ ㄹ ㅎ ㅗ ㅓ ㅏ ㅣ : " {enter}', '{shift} ㅋ ㅌ ㅊ ㅍ ㅠ ㅜ ㅡ < > ? {lang}', '{space}'],
            'jp': ['` 1 2 3 4 5 6 7 8 9 0 - = {bksp}', '{tab} た て い す か ん な に ら せ [ ] \\', '{lock} ち と し は き く ま の り ; \' {enter}', '{shift} つ さ そ ひ こ み も ね る め {lang}', '{space}'],
            'jpShift': ['~ ! @ # $ % ^ & * ( ) _ + {bksp}', '{tab} タ テ イ ス カ ン ナ ニ ラ セ { } |', '{lock} チ ト シ ハ キ ク マ ノ リ : " {enter}', '{shift} ツ サ ソ ヒ コ ミ モ ネ ル メ {lang}', '{space}']
        }, display: {
            '{bksp}': '⌫',
            '{enter}': '↵',
            '{shift}': '⇧',
            '{space}': ' ',
            '{tab}': '️Tab',
            '{lock}': '🔒',
            '{lang}': '🌐'
        }
    });

    document.querySelectorAll(".input").forEach(input => {
        input.addEventListener("click", event => {
            if (!event.target.readOnly) {
                focusedInput = event.target;
                keyboard.setInput(event.target.value);
                if (document.querySelector(".simple-keyboard") != null) {
                    document.querySelector(".simple-keyboard").style.display = "block";
                }
            }
        });
        input.addEventListener("focus", event => {
            focusedInput = event.target;
            keyboard.setInput(event.target.value);
        });
        input.addEventListener("input", event => {
            keyboard.setInput(event.target.value);
        });
    });

    document.addEventListener("click", event => {
        if (!event.target.classList.contains("input") && !event.target.closest(".simple-keyboard")) {
            let keyboard = document.querySelector("#keyboard");
            keyboard.style.display = "none";
        }
    });

    let shift = false;

    function onChange(input) {
        if (currentLanguage === 'ko') {
            if (focusedInput) {
                korean.push(input);
                focusedInput.value = Hangul.assemble(input);
            }
        } else {
            if (focusedInput) {
                focusedInput.value = input;
            }
        }
    }

    function onKeyPress(button) {
        if (shift && button !== "{shift}") {
            shift = false;
            handleShift();
        }
        if (button === "{lang}") handleLanguageSwitch();
        if (button === "{shift}") {
            shift = shift ? false : true;
            handleShift();
        }
        if (button === "{lock}") {
            handleShift();
        }
    }

    function handleLanguageSwitch() {
        const languages = ['default', 'ko', 'jp'];
        let nextIndex = (languages.indexOf(currentLanguage) + 1) % languages.length;
        switchLanguage(languages[nextIndex]);
    }

    function switchLanguage(language) {
        currentLanguage = language;
        keyboard.setOptions({
            layoutName: language
        });
    }

    function handleShift() {
        if (currentLanguage === 'default') {
            keyboard.setOptions({
                layoutName: keyboard.options.layoutName === 'default' ? 'shift' : 'default'
            });
        } else if (currentLanguage === 'ko') {
            keyboard.setOptions({
                layoutName: keyboard.options.layoutName === 'ko' ? 'koShift' : 'ko'
            });
        } else if (currentLanguage === 'jp') {
            keyboard.setOptions({
                layoutName: keyboard.options.layoutName === 'jp' ? 'jpShift' : 'jp'
            });
        }
    }
});
