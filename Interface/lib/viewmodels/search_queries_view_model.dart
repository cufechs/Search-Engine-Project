import 'package:flutter/material.dart';
import 'package:noogle_search_engine/datamodels/search_model.dart';
import 'package:noogle_search_engine/services/api.dart';
import '../dependencyInjection.dart';

class SearchViewModel extends ChangeNotifier {
  final _api = locator<Api>();
  String query = '';
  bool voiceSearch = false;

  List<SearchQueryModel>? _queries;
  List<SearchQueryModel>? get queries => _queries;

  List<SearchQueryModel>? empty = [];
  void setQuery(String str) {
    query = str;
    notifyListeners();
  }

  void setVoice(bool vs) {
    voiceSearch = vs;
    notifyListeners();
  }

  Future getSearchList(String q) async {
    if (q.isNotEmpty) {
      var queriesResult = await _api.getSearchQueries(q);

      if (queriesResult is String) {
        // show error
      } else {
        _queries = queriesResult;
      }
      return _queries;
    }
    //empty search
    return empty;
  }
}
