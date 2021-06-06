import 'package:avatar_glow/avatar_glow.dart';
import 'package:flutter/material.dart';
import 'package:noogle_search_engine/routing/route_names.dart';
import 'package:noogle_search_engine/services/navigation_service.dart';
import 'package:noogle_search_engine/viewmodels/search_queries_view_model.dart';
// ignore: import_of_legacy_library_into_null_safe
import 'package:provider/provider.dart';
// ignore: import_of_legacy_library_into_null_safe
import 'package:speech_to_text/speech_to_text.dart' as stt;

import '../../dependencyInjection.dart';

class SpeechView extends StatefulWidget {
  @override
  _SpeechViewState createState() => _SpeechViewState();
}

class _SpeechViewState extends State<SpeechView> {
  late stt.SpeechToText _speech;
  bool _isListening = false, searchVoice = false;
  String _text = 'Press the button and start speaking';
  double _confidence = 1.0;

  @override
  void initState() {
    super.initState();
    _speech = stt.SpeechToText();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(
            'Noogle Confidence: ${(_confidence * 100.0).toStringAsFixed(1)}%'),
      ),
      body: SingleChildScrollView(
        reverse: true,
        child: Column(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Container(
              padding: const EdgeInsets.fromLTRB(30.0, 30.0, 30.0, 150.0),
              child: Text(
                _text,
                style: const TextStyle(
                  fontSize: 24.0,
                  color: Colors.black,
                  fontWeight: FontWeight.w400,
                ),
              ),
            ),
            SizedBox(
              height: 150,
            ),
            Center(
              child: Row(
                children: [
                  AvatarGlow(
                    animate: _isListening,
                    glowColor: Theme.of(context).primaryColor,
                    endRadius: 75.0,
                    duration: const Duration(milliseconds: 2000),
                    repeatPauseDuration: const Duration(milliseconds: 100),
                    repeat: true,
                    child: Center(
                      child: FloatingActionButton(
                        onPressed: () {
                          _listen();
                        },
                        child: Icon(_isListening ? Icons.mic : Icons.mic_none),
                      ),
                    ),
                  ),
                  Align(
                    alignment: Alignment.centerRight,
                    child: FloatingActionButton(
                      onPressed: searchVoice &&
                              _text != 'Press the button and start speaking'
                          ? () {
                              print('search now');
                              Provider.of<SearchViewModel>(context,
                                      listen: false)
                                  .setQuery(_text);
                              Provider.of<SearchViewModel>(context,
                                      listen: false)
                                  .setVoice(true);
                              locator<NavigationService>()
                                  .navigateTo(TextRoute);
                            }
                          : null,
                      child: Icon(Icons.send),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _listen() async {
    if (!_isListening) {
      bool available = await _speech.initialize(
          onStatus: (val) => {print('status $val')},
          onError: (val) => {print('error: $val')},
          debugLogging: true);
      if (available) {
        setState(() => _isListening = true);
        setState(() => searchVoice = false);
        _speech.listen(
          onResult: (val) => setState(() {
            _text = val.recognizedWords;
            if (val.hasConfidenceRating && val.confidence > 0) {
              _confidence = val.confidence;
            }
          }),
        );
      }
    } else {
      setState(() => _isListening = false);
      setState(() => searchVoice = true);
      _speech.stop();
    }
  }
}
