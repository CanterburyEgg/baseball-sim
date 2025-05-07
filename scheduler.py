import io
import random

with open('stitch.txt') as f:
	games = f.read()

games_split = games.split('\nBREAK\n')
old_game_arr = games_split[0].split('\n')
new_game_arr = games_split[1].split('\n')
end_game_arr = []

old_teams = []
for game in old_game_arr:
	teams = game.split('\t@\t')
	old_teams.append(teams[0])
	old_teams.append(teams[1])

for game in new_game_arr:
	teams = game.split('\t@\t')
	if teams[0] not in old_teams and teams[1] not in old_teams:
		old_game_arr.append(game)
	else:
		end_game_arr.append(game)

print_str = ''

random.shuffle(old_game_arr)
for game in old_game_arr:
	print_str += game + '\n'

print_str += '\n'

random.shuffle(new_game_arr)
for game in new_game_arr:
	print_str += game + '\n'

print_str += '\n'

random.shuffle(new_game_arr)
for game in new_game_arr:
	print_str += game + '\n'

print_str += '\n'

random.shuffle(end_game_arr)
for game in end_game_arr:
	print_str += game + '\n'

with open('stitch_output.txt', 'w') as f:
	f.write(print_str)