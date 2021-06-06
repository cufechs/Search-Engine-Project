import 'package:noogle_search_engine/datamodels/search_model.dart';
import 'package:noogle_search_engine/services/api.dart';

class MockApi implements Api {
  //Mock api
  Future<dynamic> getSearchQueries(String query) async {
    return new Future.delayed(
      Duration(milliseconds: 10),
      () => createQueries(query),
    );
  }
}

createQueries(String query) {
  List<SearchQueryModel> listQueries = [];
  for (int i = 0; i < 200; i++) {
    listQueries.add(new SearchQueryModel(
      title: '$query ${i + 1}',
      description:
          'Description of query number ${i + 1} about $query apt flutter ..... Flutter , APT, loay ',
      link: 'https://www.google.com/',
    ));
  }
  return listQueries;
}
