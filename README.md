# Ultrabans - Administration Bukkit Plugin

Tired of Paranoid Plugins? Take charge of your server.

Ultrabans is an administration tool that gives the user 
more tools than a simple ban. Most of the commands
are self explanatory, considering the server aficionado
will most likely have they're own methods of administration.

This is a box'o'tools use to your discretion.

; **Economy Support** powered by [[http://dev.bukkit.org/server-mods/vault/|Vault]]\\
: Copyright (C) 2011 Morgan Humes\\
: Licensed under [[http://dev.bukkit.org/licenses/9-gnu-lesser-general-public-license-version-3-lgplv3/|LGPLv3]]\\
=== Commands ===
 **//Required Info// {} //Optional// () //Log able//* //Silent// -s**

; /ban {player} (-s) {reason}
: bans a player*
: node: ultraban.ban

; /tempban {player} (-s) {amt} {sec/min/hour/day} {Reason}
: temporarily bans a player*
: node: ultraban.tempban

; /ipban {player} (-s) {reason}
: ip bans a player*
: node: ultraban.ipban

; /unban {player}
: unbans a player*
: node: ultraban.unban

; /checkban {player}
: checks the status of a player

; /kick {player} (-s) {reason}
: kicks a player*
: node: ultraban.kick

; /warn {player} (-s) {reason}
: warns a player*
: node: ultraban.warn

; /fine {player} (-s) {amt}
: fines a player*
: node: ultraban.fine

; /empty {player}
: clears player's inventory
: node: ultraban.empty

; /forcespawn {player}
: teleports player to the default world spawn
: node: ultraban.spawn

; /editban (help)
: method of editing a stored ban

; /uhelp
: displays this help in game
: node: ultraban.help

; /ureload
: reloads the current bans
: node: ultraban.reload

; /uversion
: displays the current version
: node: ultraban.version


=== Changelog ===
; v0.0.1
++/kick,/ban,/ipban,/unban++\\
Completed Experimental Creation\\
; v0.0.2
++Added /warn with database logging++\\
++Added /fine dependency Vault++\\
__Bugfix: Reload not repopulating banlist.__\\
__Configured Op Fallback on Permissions fail__\\
; v0.0.3
--Remove Vault Dependency--\\
__Support for Permissions 3, Ex__\\
++onEnable Creates Sql Query file used to create Tables++\\
++Replaced Debug Exceptions with Directions++\\
--Removed Personalized PlayerJoin Message (Sorry)--\\
;v0.0.4
++Added /empty {player} Empties players inventory++\\
++Added /forcespawn {player} Teleport players to spawn++\\
++Added /starve {player} Remove all food from player++\\
__Tables are Autocreated__\\

=== Current Dependencies ===
;MySQL Database

=== Bugs ===
;Unable to create database with custom name in config

=== ToDo ===
Jail? Rules? Debating it.\\
Import original banlist.txt\\
Offline IP logging / Player Data (playtime)\\
Export banlist to useable banlist.txt\\
Flatfile, and SQLite support\\
Vault Permissions Integration 
or just permissions overhaul\\
Adding deleting player economy account on ban.\\
Removing player data on ban.\\
Permaban (un-unbannable {or at least from in game}) with Confirm\\