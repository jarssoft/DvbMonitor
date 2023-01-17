# DvbMonitor
Monitor teletext, EPG and other part of Dvb Trasport Stream.

- Teletext with many special data
- View multichannel subtitles like a chat. (For Finnish Broadcast Company Yle)
- TODO: EPG

## Installation

You need Java and Linux. You need also a dvb-device for receiving TV-signal and some command-line tools for opening the trasport stream.

## Use

Run dvb-zap from dvb-tools. You need -P for all PIDs.

<pre>
dvbv5-zap -c channels-v5.conf -r 'Yle TV1' -P
</pre>

Pipe the trasport stream as standar input:

<pre>
cat /dev/dvb/adapter0/dvr0 |java SubtitleMonitor
</pre>

