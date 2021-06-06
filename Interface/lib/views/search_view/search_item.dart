import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:highlight_text/highlight_text.dart';
import 'package:noogle_search_engine/datamodels/search_model.dart';
import 'package:url_launcher/url_launcher.dart';

class SearchItem extends StatefulWidget {
  final SearchQueryModel model;
  final String query;
  const SearchItem({
    Key? key,
    required this.model,
    required this.query,
  }) : super(key: key);

  @override
  _SearchItemState createState() => _SearchItemState();
}

class _SearchItemState extends State<SearchItem> {
  @override
  late Map<String, HighlightedWord> mapp;
  late LinkedHashMap<String, HighlightedWord> _highlights;
  void initState() {
    mapp = {
      '${widget.query}': HighlightedWord(
        onTap: () {},
        textStyle: const TextStyle(
          fontWeight: FontWeight.bold,
        ),
      ),
    };
    _highlights = LinkedHashMap.from(mapp);
    super.initState();
  }

  _launchURL(var urlString) async {
    var url = Uri.encodeFull(urlString);
    if (await canLaunch(url)) {
      await launch(url);
    } else {
      throw 'Could not launch $url';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Colors.white,
      elevation: 2,
      child: SizedBox(
        width: 360,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.symmetric(
                horizontal: 15.0,
                vertical: 20,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Text(
                    widget.model.title!,
                    style: TextStyle(
                      fontWeight: FontWeight.w700,
                      fontSize: 16,
                    ),
                    softWrap: true,
                  ),
                  InkWell(
                    onTap: () {
                      _launchURL(widget.model.link);
                    },
                    child: Text(
                      '${widget.model.link}',
                      style: TextStyle(
                        fontSize: 12,
                        color: Colors.blue,
                        fontStyle: FontStyle.italic,
                      ),
                    ),
                  ),
                  TextHighlight(
                    text: widget.model.description!,
                    softWrap: true,
                    words: _highlights,
                    enableCaseSensitive: false,
                    textStyle: const TextStyle(
                      fontSize: 12.0,
                      color: Colors.black,
                      fontWeight: FontWeight.w400,
                    ),
                  ),
                  // Text(
                  //   widget.model.description!,
                  //   style: TextStyle(
                  //     fontSize: 12,
                  //   ),
                  //   softWrap: true,
                  // ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
