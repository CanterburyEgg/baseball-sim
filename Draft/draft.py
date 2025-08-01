import json
import random

def ordinal(n):
    # Special handling for numbers ending in 11, 12, and 13
    if 11 <= (n % 100) <= 13:
        suffix = 'th'
    else:
        # Determine the suffix based on the last digit
        suffix = ['th', 'st', 'nd', 'rd', 'th'][min(n % 10, 4)]
    return str(n) + suffix

draft_order = []

with open('draft/order.txt') as f:
	for line in f:
		draft_order.append(line.rstrip())

with open('draft/teams.json') as f:
	teams = json.load(f)

with open('draft/players.json') as f:
	players = json.load(f)

for team, team_data in teams.items():
	team_data['drafted'] = []

for team in draft_order:
	for player, player_data in players.items():
		player_data['need'] = round(player_data['IVC'] - teams[team][player_data['Pos']] - teams[team]['drafted'].count(player_data['Pos']), 2)

	potential_picks = sorted(players.items(), key=lambda item: item[1]['need'], reverse=True)[:4]
	weights = [ 50, 30, 15, 5 ]

	drafted_player = random.choices(potential_picks, weights=weights, k=1)[0]
	del players[drafted_player[0]]

	teams[team]['drafted'].append(drafted_player[1]['Pos'])
	print(drafted_player[0])