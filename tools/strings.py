"""Writes values/strings.xml and values-ru/strings.xml for Night Train from one table.

python tools/strings.py app/src/main/res
"""
import sys
from pathlib import Path

RES = Path(sys.argv[1])

S = [
    ("app_name", "Night Train", "Ночной поезд"),
    ("save", "Save", "Сохранить"),
    ("cancel", "Cancel", "Отмена"),
    ("delete", "Delete", "Удалить"),
    ("next", "Next", "Дальше"),
    ("back", "Back", "Назад"),
    ("settings", "Settings", "Настройки"),
    ("quoted", "“%s”", "«%s»"),
    ("passenger_a", "Passenger 1", "Пассажир 1"),
    ("passenger_b", "Passenger 2", "Пассажир 2"),
    ("to_both", "Both", "Оба"),

    ("tab_station", "Station", "Вокзал"),
    ("tab_map", "Map", "Карта"),
    ("tab_tickets", "Tickets", "Билеты"),
    ("tab_album", "Album", "Альбом"),

    # Cities
    ("city_midnight", "Midnight Junction", "Полуночный"),
    ("city_pines", "Pinewood", "Сосновый Бор"),
    ("city_owls", "Owlwood", "Совиный Лес"),
    ("city_snow", "Whitefrost", "Снежин"),
    ("city_aurora", "Aurora", "Сияние"),
    ("city_mirror", "Mirrormere", "Зеркальное"),
    ("city_pass", "High Pass", "Перевал"),
    ("city_cinder", "Cinderhill", "Угольный"),
    ("city_dawn", "Dawnport", "Рассветный"),
    ("city_wormwood", "Wormwood", "Полынь"),
    ("city_starfall", "Starfall", "Звездопад"),
    ("city_vines", "Vinegrove", "Виноградное"),
    ("city_lights", "Lumen", "Огни"),
    ("city_tide", "Tidewater", "Прилив"),
    ("city_harbour", "Old Harbour", "Старая Гавань"),
    ("city_bells", "Bellmoor", "Колокольцы"),

    # Landscapes and subjects
    ("biome_forest", "pine forest", "сосновый лес"),
    ("biome_sea", "the coast", "морское побережье"),
    ("biome_lakes", "lake country", "озёрный край"),
    ("biome_mountains", "the mountains", "горы"),
    ("biome_steppe", "the steppe", "степь"),
    ("biome_snow", "the snowfields", "снега"),
    ("biome_city", "city lights", "огни городов"),
    ("biome_fields", "fields and orchards", "поля и сады"),
    ("th_childhood", "childhood", "детство"),
    ("th_family", "family", "семья"),
    ("th_past", "the past", "прошлое"),
    ("th_quirks", "quirks", "странности"),
    ("th_senses", "the senses", "ощущения"),
    ("th_fears", "fears", "страхи"),
    ("th_dreams", "dreams", "мечты"),
    ("th_values", "values", "ценности"),
    ("th_work", "work", "дело"),
    ("th_future", "the future", "будущее"),
    ("th_imagination", "imagination", "фантазия"),
    ("th_us", "the two of you", "вы двое"),

    # Trains
    ("len_local", "Local", "Электричка"),
    ("len_local_sub", "about 25 min", "минут 25"),
    ("len_express", "Express", "Скорый"),
    ("len_express_sub", "about an hour", "около часа"),
    ("len_night", "Night train", "Ночной"),
    ("len_night_sub", "the whole evening", "на весь вечер"),

    # Onboarding
    ("ob_eyebrow", "The stranger on a train", "Эффект попутчика"),
    ("ob_title", "Meet each other again, as fellow travellers", "Встретиться заново — как попутчики"),
    ("ob_text", "People tell a stranger on a train what they never tell those closest to them. Night Train gives the two of you that evening: a compartment, a polite conductor with hundreds of questions, a window full of passing landscapes, and a tunnel where the most honest things get said.",
     "Случайному попутчику в поезде рассказывают то, чего не говорят самым близким. «Ночной поезд» дарит вам двоим такой вечер: купе, вежливого проводника с сотнями вопросов, окно с бегущими пейзажами и туннель, где говорится самое честное."),
    ("ob_text2", "Put the phone on the table between you. Every trip is laid out anew: the line, the stations with their rules, the passengers who sit down for a while, the restaurant car. No question comes twice until you have heard them all.",
     "Положите телефон на стол между вами. Каждый рейс складывается заново: линия, станции со своими правилами, попутчики, которые подсаживаются ненадолго, вагон-ресторан. Ни один вопрос не повторится, пока не прозвучат все."),
    ("ob_names_title", "Who's travelling?", "Кто едет?"),
    ("ob_names_text", "Names show whose turn it is. Leave them blank to travel as Passenger 1 and Passenger 2.", "По именам видно, чья очередь отвечать. Можно оставить пустыми — тогда вы Пассажир 1 и Пассажир 2."),
    ("ob_seat_title", "How do you sit?", "Как вы сидите?"),
    ("ob_seat_text", "You can change it any time in the settings.", "Это можно поменять в настройках."),
    ("ob_board", "Board the train", "В вагон"),
    ("seat_opposite", "Facing each other", "Друг напротив друга"),
    ("seat_opposite_sub", "The phone lies on the table between you. The screen splits in two, each half turned to whoever sits on that side.", "Телефон лежит на столе между вами. Экран делится пополам, и каждая половина повёрнута к своему человеку."),
    ("seat_side", "Side by side", "Рядом"),
    ("seat_side_sub", "One screen, one way up, for reading together.", "Один экран, одна сторона — читаете вместе."),

    # Station
    ("st_here", "You are at", "Вы на станции"),
    ("st_ongoing", "Train underway", "Поезд в пути"),
    ("st_stop_of", "Stop %1$d of %2$d", "Остановка %1$d из %2$d"),
    ("st_back_in", "Back to the compartment", "Вернуться в купе"),
    ("st_buy", "Buy a ticket", "Купить билет"),
    ("st_notes_sub", "Waiting for: %s. Found at the next boarding.", "Ждёт: %s. Найдётся при посадке."),
    ("st_from_here", "Trains from here", "Отсюда идут поезда"),
    ("st_new", "new", "впервые"),
    ("st_log", "Travel log", "Путевой журнал"),
    ("stat_trips", "Trips", "Рейсы"),
    ("stat_cities", "Cities", "Города"),
    ("stat_guessed", "Guessing", "Угадываете"),
    ("stat_asked", "%1$d of %2$d questions heard so far. None comes again until all of them have.", "Прозвучало %1$d из %2$d вопросов. Ни один не повторится, пока не прозвучат все."),
    ("stat_questions", "Questions", "Вопросы"),
    ("stat_holes", "Guessed", "Угадано"),
    ("stat_dining", "Alike", "Совпало"),

    # Ticket office
    ("board_title", "Ticket office", "Касса"),
    ("board_where", "Where to", "Куда"),
    ("board_train", "Which train", "Какой поезд"),
    ("board_line_note", "The line runs through %1$s. The conductor's subjects: %2$s.", "Линия идёт через %1$s. Темы проводника: %2$s."),
    ("board_sleeper", "Sleeping car (18+)", "Спальный вагон (18+)"),
    ("board_sleeper_sub", "Offered after the tunnel, if both of you agree.", "Будет предложен после туннеля, если оба согласятся."),
    ("board_go", "Board the train", "Сесть в поезд"),

    # Ticket
    ("ticket_head", "Night Train · sleeping car", "Ночной поезд · купе"),
    ("ticket_car", "Car", "Вагон"),
    ("ticket_seats", "Seats", "Места"),
    ("ticket_cards", "Along the way", "В пути"),
    ("ticket_delete_title", "Delete this trip?", "Удалить рейс?"),
    ("ticket_delete_text", "The ticket and everything on it will be gone, its postcards too.", "Пропадут билет и всё, что было в пути, вместе с открытками."),
    ("trip_through_tunnel", "through the tunnel", "через туннель"),
    ("trip_through_sleeper", "with the sleeping car", "со спальным вагоном"),

    # The trip
    ("stage_evening", "Evening", "Вечер"),
    ("stage_night", "Night", "Ночь"),
    ("stage_tunnel", "Tunnel", "Туннель"),
    ("stage_dawn", "Dawn", "Рассвет"),
    ("who_both", "Both of you", "Вам обоим"),
    ("who_you", "For you, %s", "Вам, %s"),
    ("who_answers", "%s answers", "Отвечает: %s"),
    ("who_guesses", "%s guesses", "Угадывает: %s"),
    ("q_conductor", "The conductor asks", "Спрашивает проводник"),
    ("q_sleeper", "Sleeping car", "Спальный вагон"),
    ("act_brake", "Brake", "Стоп-кран"),
    ("act_deeper", "Deeper", "Глубже"),
    ("act_return", "Return", "Обратно"),
    ("act_keep", "Keep", "На память"),
    ("keep_title", "Keep as a postcard", "Сохранить открытку"),
    ("keep_text", "It goes into the album. Add a line about what was said if you like: it will be good to read in a year.", "Она ляжет в альбом. Можно добавить строчку о том, что прозвучало: через год будет приятно перечитать."),
    ("keep_hint", "What was said…", "Что прозвучало…"),
    ("keep_go", "Keep", "Сохранить"),
    ("echo_eyebrow", "The same train", "Тот же поезд"),
    ("echo_when", "You answered this one on %s.", "На этот вопрос вы уже отвечали: %s."),
    ("echo_ask", "What has changed since then?", "Что изменилось с тех пор?"),
    ("guess_eyebrow", "Guess", "Угадайте"),
    ("guess_first", "%1$s, say out loud what %2$s will answer, before they do.", "%1$s, скажите вслух, какой будет ответ. Отвечает: %2$s."),
    ("guess_now", "%s, now your answer.", "%s, теперь ваш ответ."),
    ("guess_said", "Guess is in. The answer!", "Догадка сказана. Ответ!"),
    ("guess_hit", "Spot on", "В точку"),
    ("guess_near", "Close", "Почти"),
    ("guess_miss", "Missed", "Мимо"),
    ("guess_miss_note", "A hit punches a hole in your ticket. A miss is something new about the person opposite.", "Попадание — дырочка в билете. Промах — что-то новое о человеке напротив."),
    ("rule_eyebrow", "Station", "Станция"),
    ("rule_span", "The rule holds for the next %d questions.", "Правило действует следующие %d вопроса."),
    ("rule_go", "Off we go", "Поехали"),
    ("rs_mirror", "Mirror Halt", "Зеркальная"),
    ("rs_three", "Three Words", "Три Слова"),
    ("rs_child", "Ten Summers", "Десятилетка"),
    ("rs_whisper", "Hush", "Тихая"),
    ("rs_both", "Crossing", "Разъезд"),
    ("rs_eyes", "Long Look", "Смотровая"),
    ("rs_hands", "Handfast", "Ладони"),
    ("rs_silence", "Still Water", "Тихий Омут"),
    ("rs_lie", "Crooked Lanes", "Кривые Дорожки"),
    ("rule_mirror", "Answer as the person opposite would. Then they put you right.", "Отвечайте так, как ответил бы человек напротив. Потом человек напротив вас поправит."),
    ("rule_three", "Three words first, exactly three. Then the whole story.", "Сначала ровно три слова. Потом — вся история."),
    ("rule_child", "First answer as you would have at ten, then as you would now.", "Сначала ответьте так, как ответили бы в десять лет, а потом — как сейчас."),
    ("rule_whisper", "Answer in a whisper, leaning closer.", "Отвечайте шёпотом, наклонившись поближе."),
    ("rule_both", "Every question goes to both of you, one after the other.", "Каждый вопрос — вам обоим, по очереди."),
    ("rule_eyes", "Hold each other's eyes while you answer. No looking away.", "Отвечая, смотрите друг другу в глаза. Не отводите взгляд."),
    ("rule_hands", "Hold hands for this whole stretch.", "Держитесь за руки весь этот перегон."),
    ("rule_silence", "Ten seconds of silence before each answer. Let it surface.", "Перед каждым ответом — десять секунд тишины. Пусть всплывёт."),
    ("rule_lie", "One answer on this stretch will be untrue. Decide in secret who lies. At the end you will guess where.", "Один ответ на этом перегоне будет неправдой. Кто солжёт — решите втайне. В конце будете угадывать."),
    ("rule_mirror_short", "Mirror Halt: answer for the person opposite", "Зеркальная: ответьте за человека напротив"),
    ("rule_three_short", "Three Words: three words first", "Три Слова: сначала ровно три слова"),
    ("rule_child_short", "Ten Summers: at ten, then now", "Десятилетка: сначала в десять лет, потом сейчас"),
    ("rule_whisper_short", "Hush: in a whisper", "Тихая: шёпотом"),
    ("rule_both_short", "Crossing: both answer", "Разъезд: отвечают оба"),
    ("rule_eyes_short", "Long Look: eyes on each other", "Смотровая: глаза в глаза"),
    ("rule_hands_short", "Handfast: hold hands", "Ладони: держитесь за руки"),
    ("rule_silence_short", "Still Water: ten seconds of silence first", "Тихий Омут: сначала десять секунд тишины"),
    ("rule_lie_short", "Crooked Lanes: one answer here is untrue", "Кривые Дорожки: один ответ здесь — неправда"),
    ("halt_eyebrow", "Halt", "Полустанок"),
    ("halt_name", "%1$s %2$s", "%1$s %2$s"),
    ("halt_lead", "Two minutes' stop. Off the train, both of you:", "Стоянка две минуты. Выходите оба:"),
    ("halt_skip", "Stay aboard", "Остаться в вагоне"),
    ("halt_done", "Done", "Сделано"),
    ("dining_eyebrow", "Restaurant car", "Вагон-ресторан"),
    ("dining_lead", "On three, both show your pick at once: one finger or two.", "На счёт «три» оба одновременно покажите выбор: один палец или два."),
    ("dining_count", "One, two…", "Раз, два…"),
    ("dining_show", "Show!", "Три!"),
    ("dining_match", "Same", "Совпало"),
    ("dining_split", "Different", "Разошлись"),
    ("dining_total", "%1$d of %2$d alike", "Совпало %1$d из %2$d"),
    ("dining_high", "A good table: you would share a dessert.", "Хороший столик: вы бы взяли один десерт на двоих."),
    ("dining_low", "Different tastes. Ask each other why: that is the best part of dinner.", "Вкусы разные. Спросите друг друга почему — это самое вкусное."),
    ("dining_leave", "Back to the compartment", "Вернуться в купе"),
    ("stranger_eyebrow", "A fellow passenger", "Попутчик"),
    ("stranger_yes", "Please, sit down", "Присаживайтесь"),
    ("stranger_no", "Seat's taken", "Занято"),
    ("inspector_time", "Time!", "Время!"),
    ("st_sailor", "An old sailor", "Старый моряк"),
    ("st_child", "A girl with a kitten", "Девочка с котёнком"),
    ("st_seer", "A fortune teller", "Гадалка"),
    ("st_poet", "A poet", "Поэт"),
    ("st_inspector", "The ticket inspector", "Ревизор"),
    ("st_granny", "A granny with pies", "Бабушка с пирожками"),
    ("st_traveller", "A time traveller", "Путешественник во времени"),
    ("st_monk", "A silent monk", "Молчаливый монах"),
    ("st_sailor_intro", "“Mind if I sit? The sea taught me to ask straight.”", "«Позволите присесть? Море приучило меня спрашивать напрямик»."),
    ("st_child_intro", "“Mum's asleep. Can I ask you things? Lots of things?”", "«Мама спит. Можно я вас поспрашиваю? Много-много?»"),
    ("st_seer_intro", "“The cards fell out on their own the moment you came in.”", "«Карты сами выпали, как только вы вошли»."),
    ("st_poet_intro", "“Forgive me, I collect details. Lend me a few of yours.”", "«Простите, я собираю детали. Одолжите мне несколько ваших»."),
    ("st_inspector_intro", "“Tickets! And answers. Five seconds each. Quickly now.”", "«Билетики! И ответы. Пять секунд на каждый. Живо»."),
    ("st_granny_intro", "“Take a pie, dears, and tell me about your people.”", "«Берите пирожок, милые, и рассказывайте про своих»."),
    ("st_traveller_intro", "“I'm from a year you'll both remember. I can't say which.”", "«Я из года, который вы оба запомните. Какого — сказать не могу»."),
    ("st_monk_intro", "He bows, lays a card on the table and smiles. Answer without words.", "Он кланяется, кладёт на стол карточку и улыбается. Отвечать — без слов."),
    ("tunnel_eyebrow", "A tunnel ahead", "Впереди туннель"),
    ("tunnel_title", "In the dark, the most honest things get said", "В темноте говорится самое честное"),
    ("tunnel_text", "If you are both ready, each keep a thumb on your lamp. If not, the train goes round, and nobody asks why.", "Если оба готовы, держите палец каждый на своём фонаре. Если нет — поезд пойдёт в объезд, и никто не спросит почему."),
    ("tunnel_no", "Go round", "В объезд"),
    ("sleeper_eyebrow", "Sleeping car · 18+", "Спальный вагон · 18+"),
    ("sleeper_title", "The sleeping car is open", "Спальный вагон открыт"),
    ("sleeper_text", "A few questions about desire and closeness. You go in only if both thumbs are down; either of you can pass it by.", "Несколько вопросов о желании и близости. Входите, только если держат оба; пройти мимо может любой."),
    ("sleeper_no", "Pass by", "Мимо"),
    ("gate_hold", "Hold", "Держите"),
    ("gate_holding", "Holding…", "Держу…"),
    ("note_for", "For %s", "Для: %s"),
    ("note_eyebrow", "A note in the compartment", "Записка в купе"),
    ("note_found", "For: %1$s. From: %2$s.", "Для: %1$s. От: %2$s."),
    ("note_not_yours", "This note is for %s. No peeking: yours may come next time.", "Эта записка для: %s. Не подглядывайте — ваша, может быть, в следующий раз."),
    ("note_read", "Fold it away", "Сложить и убрать"),
    ("lie_eyebrow", "Crooked Lanes: the reveal", "Кривые Дорожки: разоблачение"),
    ("lie_text", "Which answer on that stretch was not true?", "Какой ответ на том перегоне был неправдой?"),
    ("lie_sub", "Each of you names a guess. Then the liar owns up and gives the true answer.", "Каждый называет свою догадку. Потом — признание и правдивый ответ."),
    ("lie_go", "Owned up", "Признались"),
    ("trip_leave", "Leave the compartment; the trip will wait", "Выйти из купе; рейс подождёт"),
    ("trip_terminus", "Get off at the next station", "Сойти на ближайшей"),
    ("trip_terminus_title", "Get off early?", "Сойти раньше?"),
    ("trip_terminus_text", "The train pulls into %s now and the rest of the route is let go.", "Поезд сразу прибудет: %s. Остаток маршрута отменится."),
    ("trip_terminus_go", "Arrive now", "Прибыть"),
    ("arr_eyebrow", "Arrived at", "Прибытие"),
    ("arr_new", "A new city on your map!", "Новый город на вашей карте!"),
    ("arr_notes", "A note in the compartment", "Записка в купе"),
    ("arr_notes_sub", "Leave a few words for the other one. They will find them at the next boarding, and not before.", "Оставьте друг другу пару слов. Записку найдут при следующей посадке, не раньше."),
    ("arr_note_from", "From %s", "От: %s"),
    ("arr_note_title", "A note for %s", "Записка для: %s"),
    ("arr_note_text", "Write it while they look away. It stays folded until the next trip.", "Пишите, пока человек напротив отвернулся. Записка останется сложенной до следующего рейса."),
    ("arr_note_hint", "A few words…", "Пара слов…"),
    ("arr_off", "Step onto the platform", "Выйти на перрон"),
    ("res_hit", "spot on", "в точку"),
    ("res_near", "close", "почти"),
    ("res_miss", "missed", "мимо"),
    ("res_match", "alike", "совпало"),
    ("res_split", "different", "разошлись"),
    ("res_skipped", "brake", "стоп-кран"),

    # Map
    ("map_eyebrow", "The railway", "Железная дорога"),
    ("map_title", "Map of the lines", "Карта путей"),
    ("map_sub", "%1$d of %2$d cities · %3$d of %4$d lines travelled", "Городов: %1$d из %2$d · путей: %3$d из %4$d"),
    ("map_here", "Where the train stands", "Где стоит поезд"),
    ("map_city", "City", "Город"),
    ("map_train_here", "Your train stands here.", "Ваш поезд стоит здесь."),
    ("map_never", "Not reached yet.", "Здесь вы ещё не были."),
    ("map_go", "Ticket to %s", "Билет: %s"),
    ("map_far", "No direct train from where you stand.", "Отсюда прямого поезда нет."),
    ("map_legend", "Landscapes and their subjects", "Пейзажи и их темы"),

    # Tickets
    ("tickets_eyebrow", "Ticket book", "Билетная книжка"),
    ("tickets_title", "Your trips", "Ваши рейсы"),
    ("tickets_empty", "No tickets yet", "Билетов пока нет"),
    ("tickets_empty_sub", "Every trip leaves its ticket here, punched once for every guess that hit.", "Каждый рейс оставит здесь билет — с дырочкой за каждую угаданную догадку."),
    ("trend_title", "Guessing each other", "Угадываете друг друга"),
    ("trend_sub", "hits, trip by trip", "попадания по рейсам"),

    # Album
    ("album_eyebrow", "Album", "Альбом"),
    ("album_title", "Postcards", "Открытки"),
    ("album_cities", "Cities · %1$d of %2$d", "Города · %1$d из %2$d"),
    ("album_kept", "Answers kept", "Сохранённые ответы"),
    ("album_empty", "No postcards yet", "Открыток пока нет"),
    ("album_empty_sub", "Tap the star under a question to keep an answer, with a line about what was said.", "Нажмите звёздочку под вопросом, чтобы сохранить ответ — со строчкой о том, что прозвучало."),

    # Settings
    ("set_passengers", "Passengers", "Пассажиры"),
    ("set_seating", "Seating", "Как вы сидите"),
    ("set_train", "On the train", "В поезде"),
    ("set_sound", "Wheels on the rails", "Стук колёс"),
    ("set_sound_sub", "A quiet rumble while the train moves.", "Тихий перестук, пока поезд едет."),
    ("set_haptics", "Vibration", "Вибрация"),
    ("set_haptics_sub", "A buzz for a guess that hits and for a gate that opens.", "Отклик на угаданный ответ и на открытые ворота."),
    ("set_sleeper", "Sleeping car (18+)", "Спальный вагон (18+)"),
    ("set_sleeper_sub", "Questions about desire and closeness, offered after the tunnel when both of you agree.", "Вопросы о желании и близости — после туннеля, если оба согласны."),
    ("set_language", "Language", "Язык"),
    ("set_lang_system", "System", "Как в системе"),
    ("set_data", "Your data", "Данные"),
    ("backup_export", "Export a backup", "Сохранить копию"),
    ("backup_export_sub", "Every trip, postcard and note in one file", "Все рейсы, открытки и записки в одном файле"),
    ("backup_import", "Restore from a backup", "Восстановить из копии"),
    ("backup_import_sub", "Replaces what's on this phone", "Заменит то, что сейчас на телефоне"),
    ("backup_import_title", "Restore this backup?", "Восстановить копию?"),
    ("backup_import_text", "Every trip, postcard and note on this phone will be replaced by the ones in the file.", "Все рейсы, открытки и записки на этом телефоне заменятся теми, что в файле."),
    ("backup_import_go", "Restore", "Восстановить"),
    ("backup_done", "Backup saved", "Копия сохранена"),
    ("backup_failed", "Couldn't save the backup", "Не удалось сохранить копию"),
    ("backup_restored", "Restored", "Восстановлено"),
    ("backup_bad", "That isn't a Night Train backup", "Это не копия «Ночного поезда»"),
    ("set_about", "About", "О приложении"),
    ("set_method", "The questions deepen as the night goes on, after Arthur Aron's study of how gradual, mutual openness brings people closer. Guessing each other's answers is the idea of John Gottman's love maps: knowing the other person's inner world, and keeping that knowledge up to date.",
     "Вопросы становятся глубже по ходу ночи — по следам исследования Артура Арона о том, как постепенная взаимная откровенность сближает людей. Угадывание ответов — идея «карт любви» Джона Готтмана: знать внутренний мир другого и обновлять это знание."),
    ("set_deck", "The deck holds %d cards. No question repeats until you have heard them all.", "В колоде %d карточек. Ни один вопрос не повторится, пока не прозвучат все."),
    ("set_privacy", "No account, no cloud, no ads and no internet permission. Everything stays on this phone.", "Без аккаунта, облака, рекламы и доступа в интернет. Всё остаётся на этом телефоне."),
]

PLURALS = [
    ("notes_waiting", {"one": "%d note waits in the compartment", "other": "%d notes wait in the compartment"},
     {"one": "%d записка ждёт в купе", "few": "%d записки ждут в купе", "many": "%d записок ждут в купе", "other": "%d записки ждут в купе"}),
    ("postcards_kept", {"one": "%d postcard kept", "other": "%d postcards kept"},
     {"one": "%d открытка на память", "few": "%d открытки на память", "many": "%d открыток на память", "other": "%d открытки на память"}),
    ("arrivals", {"one": "You've arrived here %d time.", "other": "You've arrived here %d times."},
     {"one": "Вы приезжали сюда %d раз.", "few": "Вы приезжали сюда %d раза.", "many": "Вы приезжали сюда %d раз.", "other": "Вы приезжали сюда %d раза."}),
]

ARRAYS = [
    ("halt_adj", ["Little", "Upper", "Lower", "Old", "Quiet", "Far", "New", "Red", "White", "Sleepy"],
     ["Малые", "Верхние", "Нижние", "Старые", "Тихие", "Дальние", "Новые", "Красные", "Белые", "Сонные"]),
    ("halt_noun", ["Elms", "Pines", "Springs", "Ponds", "Meadows", "Hills", "Bridges", "Stones", "Willows", "Wells"],
     ["Вязы", "Сосны", "Ключи", "Пруды", "Луга", "Холмы", "Мосты", "Камни", "Ивы", "Колодцы"]),
]


def esc(s: str) -> str:
    s = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "\\'").replace('"', '\\"')
    if s[:1] in "@?":
        s = "\\" + s
    return s


def write(lang: int, folder: str):
    out = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
    for key, en, ru in S:
        if lang == 1 and key == "app_name":
            pass
        out.append(f'    <string name="{key}">{esc([en, ru][lang])}</string>')
    for key, en, ru in PLURALS:
        out.append(f'    <plurals name="{key}">')
        for q, t in [en, ru][lang].items():
            out.append(f'        <item quantity="{q}">{esc(t)}</item>')
        out.append("    </plurals>")
    for key, en, ru in ARRAYS:
        out.append(f'    <string-array name="{key}">')
        for t in [en, ru][lang]:
            out.append(f"        <item>{esc(t)}</item>")
        out.append("    </string-array>")
    out.append("</resources>")
    (RES / folder).mkdir(parents=True, exist_ok=True)
    (RES / folder / "strings.xml").write_text("\n".join(out) + "\n", encoding="utf-8")


keys = [k for k, *_ in S]
assert len(keys) == len(set(keys)), [k for k in keys if keys.count(k) > 1]
write(0, "values")
write(1, "values-ru")
print(len(S), "strings")
