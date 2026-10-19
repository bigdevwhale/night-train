#!/usr/bin/env bash
# Takes the README screenshots on the test phone from the screenshot build, in one language.
#
#   ./gradlew assembleDebug -Pdemo -Pshots && adb install -r app/build/outputs/apk/debug/app-debug.apk
#   bash tools/capture.sh docs/screenshots/en en
#
# Only ever clears app.nighttrain.shots (its own package, never a real install or a demo someone is
# trying), then rides one express trip and photographs the first stop of each kind.
set -u
cd "$(dirname "$0")/.."
source tools/phone.sh
OUT="$1"
LANG_="$2"
P=app.nighttrain.shots
mkdir -p "$OUT"
SHOTS="$OUT"

if [ "$LANG_" = ru ]; then
  L_BUY="Купить билет"; L_BOARD="Сесть в поезд"; L_NEXT="Дальше"; L_GO="Поехали"; L_DONE="Сделано"; L_COUNT="Раз, два…"
  L_SAME="Совпало"; L_SIT="Присаживайтесь"; L_GUESS="Догадка сказана. Ответ!"; L_HIT="В точку"; L_ROUND="В объезд"
  L_BACK="Вернуться в купе"; L_FOLD="Сложить и убрать"; L_OWN="Признались"; L_OFF="Выйти на перрон"
  L_MAP="Карта"; L_TICKETS="Билеты"; L_ALBUM="Альбом"
else
  L_BUY="Buy a ticket"; L_BOARD="Board the train"; L_NEXT="Next"; L_GO="Off we go"; L_DONE="Done"; L_COUNT="One, two…"
  L_SAME="Same"; L_SIT="Please, sit down"; L_GUESS="Guess is in. The answer!"; L_HIT="Spot on"; L_ROUND="Go round"
  L_BACK="Back to the compartment"; L_FOLD="Fold it away"; L_OWN="Owned up"; L_OFF="Step onto the platform"
  L_MAP="Map"; L_TICKETS="Tickets"; L_ALBUM="Album"
fi

"$ADB" shell am force-stop $P
"$ADB" shell pm clear $P >/dev/null
"$ADB" shell cmd locale set-app-locales $P --locales "$LANG_" >/dev/null 2>&1
"$ADB" shell monkey -p $P -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
sleep 7

shot 1-station
tap_text $P "$L_BUY" || exit 1
sleep 2
shot 2-board
tap_text $P "$L_BOARD" || exit 1
sleep 3

taken=" "
take() { case "$taken" in *" $1 "*) ;; *) shot "$2"; taken="$taken$1 " ;; esac; }
has() { echo "$T" | grep -qxF "$1"; }
for i in $(seq 1 45); do
  T=$(texts $P) || exit 1
  if has "$L_OFF"; then sleep 1; shot 8-arrival; break; fi
  if has "$L_FOLD"; then tap_text $P "$L_FOLD"
  elif has "$L_GUESS"; then take guess 4-guess; tap_text $P "$L_GUESS"; sleep 1; tap_text $P "$L_HIT"
  elif has "$L_COUNT"; then take dining 5-dining; tap_text $P "$L_COUNT"; sleep 3; tap_text $P "$L_SAME"
  elif has "$L_SIT"; then take stranger 6-stranger; tap_text $P "$L_SIT"
  elif has "$L_ROUND"; then take tunnel 7-tunnel; tap_text $P "$L_ROUND"
  elif has "$L_GO"; then take rule 9-station; tap_text $P "$L_GO"
  elif has "$L_DONE"; then tap_text $P "$L_DONE"
  elif has "$L_OWN"; then tap_text $P "$L_OWN"
  elif has "$L_BACK"; then tap_text $P "$L_BACK"
  elif has "$L_NEXT"; then take question 3-question; tap_text $P "$L_NEXT"
  else echo "stuck:"; echo "$T"; exit 1
  fi
  sleep 1.3
done
tap_text $P "$L_OFF" || exit 1
sleep 2
tap_text $P "$L_MAP" && sleep 2 && shot 10-map
tap_text $P "$L_TICKETS" && sleep 2 && shot 11-tickets
tap_text $P "$L_ALBUM" && sleep 2 && shot 12-album
echo "done: $OUT"
