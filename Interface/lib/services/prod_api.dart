import 'package:http/http.dart' as http;
import 'dart:convert';

import 'package:noogle_search_engine/datamodels/search_model.dart';
import 'package:noogle_search_engine/services/api.dart';

class ProdApi implements Api {
  //Production api
  //Uri.parse('http://192.168.1.8:8080/api/search/loa'),

  Future<dynamic> getSearchQueries(String query) async {
    final response = await http.get(
        //Uri.http('localhost:8080', '/api/search/loay'),
        Uri.parse(
            'http://127.0.0.1:8000/http://127.0.0.1:8080/api/search/$query'),
        //Uri.parse('http://192.168.1.8:8080/api/search/loa'),
        headers: {
          'Access-Control-Allow-Origin': '*',
          "Access-Control-Allow-Methods": "GET, HEAD"
        });
    if (response.statusCode == 200) {
      List<SearchQueryModel> queries = (jsonDecode(response.body) as List)
          .map((query) => SearchQueryModel.fromJson(query))
          .toList();
      return queries;
    }

    // something wrong happened
    return 'Could not fetch the queris at this time';
  }
}
