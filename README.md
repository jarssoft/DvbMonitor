# DvbMonitor

Extract teletext and other stuff from MPEG Trasport Stream.

- Monitoring teletext with many special data. (Monitor-class)
- View teletext subtitles like a chat. (SubtitleMonitor-class)
- TODO: EPG

## Installation

DvbMonitor runs on Java Runtime Environment on the Linux system. To receive television signal as Transport Stream you need the device and software for tuning.

## Using

Make 'zapping'. For example run the dvb-zap from dvb-tools. You need to use -P to get all PIDs of the stream. For example:

<pre>
dvbv5-zap -c channels-v5.conf 610000000 -P 
</pre>

Then use /dev/dvb/adapter0/dvr0 as standard input:

<pre>
cat /dev/dvb/adapter0/dvr0 |java Monitor
</pre>

Or use [dvbsnoop](https://dvbsnoop.sourceforge.net/) to get only one PID:

<pre>
dvbsnoop -s ts -b 5000 |java Monitor
</pre>

Or get transport stream from file:

<pre>
cat koe.ts |java Monitor
</pre>

## SubtitleMonitor

Here is example of reading teletext subtitle from multiple television channel.

![Kuvakaappaus2023-01-17 10-17-39](https://user-images.githubusercontent.com/24640011/213861529-63afb220-b341-4f4c-9413-e7e0f0ac1dfb.png)
