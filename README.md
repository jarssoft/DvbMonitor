# DvbMonitor

A small software for monitoring teletext, EPG and other part of DVB Trasport Stream.

- Teletext with many special data
- View multichannel subtitles like a chat. (For Finnish Broadcast Company Yle)
- TODO: EPG

## Installation

DvbMonitor runs on Java Runtime Environment on the Linux system. To receive television signal as Transport Stream you needs also the device and software for tuning.

## Using

Make 'zapping'. For example run the dvb-zap from dvb-tools. You need to use -P to get all PIDs of the stream.

<pre>
dvbv5-zap -c channels-v5.conf -r 'Yle TV1' -P
</pre>

Then make a pipe where the trasport stream sends as standard input:

<pre>
cat /dev/dvb/adapter0/dvr0 |java SubtitleMonitor
</pre>

Or use dvbsnoop to get certain PID:

<pre>
dvbsnoop -s ts -nph -n 100000000 -b 5010 |java Monitor
</pre>