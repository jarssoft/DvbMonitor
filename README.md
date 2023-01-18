# DvbMonitor

Extract teletext, EPG and other from MPEG Trasport Stream.

- Monitoring teletext with many special data. (Monitor-class)
- View teletext subtitles from multiple tv-channel like a chat. (SubtitleMonitor-class)
- TODO: EPG

## Installation

DvbMonitor runs on Java Runtime Environment on the Linux system. To receive television signal as Transport Stream you needs also the device and software for tuning.

## Using

Make 'zapping'. For example run the dvb-zap from dvb-tools. You need to use -P to get all PIDs of the stream.

<pre>
dvbv5-zap -c channels-v5.conf -r 'Yle TV1' -P
</pre>

Then make a pipe and put trasport stream as standard input:

<pre>
cat /dev/dvb/adapter0/dvr0 |java SubtitleMonitor
</pre>

Or use dvbsnoop to get certain PID:

<pre>
dvbsnoop -s ts -nph -n 100000000 -b 5010 |java Monitor
</pre>

Or from file:

<pre>
cat koe.ts |java SubtitleMonitor
</pre>