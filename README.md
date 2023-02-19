# DvbMonitor

Extracts teletext and other stuff from MPEG Trasport Stream binary data.

- Teletext/Monitor: A simple teletext monitoring.
- Teletext/SubtitleMonitor: Views teletext subtitles like a chat.
- EPG/*: Decodes Event Information Table
- TODO: View thumbnails from video
- TODO: Audio only television

## Installation

DvbMonitor runs on Java Runtime Environment (at least OpenJDK 11 and 17) on the Linux system. To receive television signal as Transport Stream you need the device and software for tuning.

## Using

Make 'zapping'. For example run the dvb-zap from dvb-tools. You need to use -P to get all PIDs of the stream. For example:

<pre>
dvbv5-zap -c channels-v5.conf 610000000 -P 
</pre>

Then use /dev/dvb/adapter0/dvr0 as standard input,

<pre>
cat /dev/dvb/adapter0/dvr0 |java -ea EPG/Monitor
</pre>

...or use [dvbsnoop](https://dvbsnoop.sourceforge.net/) to get only one PID,

<pre>
dvbsnoop -s ts -b 0x12 |java -ea EPG/Monitor
</pre>

...or give it an example transport stream from file:

<pre>
cat ../test.ts |java -ea EPG/Monitor
</pre>

## SubtitleMonitor

Here is an example of reading subtitles from multiple television channel and put them like a chat. It also grouping text lines to paragraphs. It use header format <Channel Name / Program Name>.

![Screenshot](https://www.ohjelmakartta.fi/github/screenshot.jpg)

## Read more

- [Wikipedia: MPEG_transport_stream](https://en.wikipedia.org/wiki/MPEG_transport_stream)

- [ETSI EN 300 706 Enhanced Teletext specification](https://www.etsi.org/deliver/etsi_en/300700_300799/300706/01.02.01_60/en_300706v010201p.pdf),  V1.2.1 (2003-04),  European Telecommunications Standards Institute

- [ETSI EN 300 468 Digital Video Broadcasting (DVB); Specification for Service Information (SI) in DVB systems](https://www.etsi.org/deliver/etsi_en/300400_300499/300468/01.17.01_20/en_300468v011701a.pdf), V1.17.1 (2022-07)
