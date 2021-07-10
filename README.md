Сервер взаимодействуте с одним экземпляром нейросети. С помощью post запросов к host:port/api можно будет взаимодействовать с серсером. Сервер так же имеет встроенный перевод текста на русский язык с помощью яндекс переводчика. Если он включен, то общая задержка ответа увеличивается

Шаблон запроса такой:
{ command: "command_name", args: [ "arg1", "arg2", ... ] }

Список команд:
1) status - возвращает текйщий статус нейросети. В json будет параметр status, который будет равен одному из возможных статусов.

Возможные статусы:
not_started - сеть не запущена. Чтобы её запустить, вызовите команду launch
not_response - сеть запущена, но не отвечает. Может возникать в процессе её загрузки после вызова launch, или когда она генерирует новое предложение. Если статус существует дольше 60 секунд, то можно насильно перезагрузить сеть командой reload
in_menu - сеть ожидает загрузки сценария или его создания. Если хотите загрузить сценарий, то вызовите команду load_scene, если хотите создать новый, то команду new_scene_preset или new_scene. Список сохраненных игр лежит в ответе в save_games, список из пресетов сценариев лежит в scene_presets. В scene_presets лежат не названия сценариев, а списки с названиями, потому что они сгрупированы по теме.
{
	status: "in_menu",
	save_games: [ "game1", "game2", "superGame_3" ],
	scene_presets: 
	[
		{
			theme: "theme_name",
			presets: [ "preset0", "preset1", "preset2" ]
		}
	]
}

wait_response - ожидает ответа человека на её предложение. Дополнительно содежрит параметр message со значением предложения от нейросети. Для того, чтобы дать ответ, вызовите команду send_response

2) launch - запускает нейросеть, если она еще не запущена. После вызова она будет загружатся в течении 30-60 секунд.
3) reload - насильно перезапускает сеть, если она запущена.
4) load_scene - если сеть в меню, то загружает указанную сцену из сохраненных
args: [ "название сцены" ]
5) save_scene - сохраняет текущую запущенную сцену
6) new_scene_preset - создает новую сцену из пресета. Вторым агрументом указывается индекс темы пресета, третьим - индекс пресета внутри темы
args: [ "scene_name", "theme_index", "preset_index" ]
7) new_scene - создает новую кастомную сцену
args: [ "scene_name", "context", "promt" ]
context - это набор знаний о мире, это то, что нейросеть не будет забывать в процессе
promt - это начальное сообщение для нейросети, она его может потом в последствии забыть
8) send_response - отправляет сообщение нейросети в ответ на её. Первый аргумент - само сообщение, а второй аргумент указывает его тип. Если тип равен say, то вы разговариваете с объектом, если do, то взаимодействуете с миром внутри
args: [ "message", "say or do" ]
9) toggle_translate - включает/выключает перевод текста на русский, как ввод, так и вывод
args: [ "enable or disable" ]