import 'package:flutter/material.dart';
import 'package:noogle_search_engine/viewmodels/search_queries_view_model.dart';
import 'package:noogle_search_engine/views/search_view/search_item.dart';
import 'package:noogle_search_engine/widgets/shade_loading.dart';
// ignore: import_of_legacy_library_into_null_safe
import 'package:provider/provider.dart';

class SearchView extends StatefulWidget {
  //final int pageNo;

  //const SearchView({this.pageNo = 1});
  @override
  _SearchViewState createState() => _SearchViewState();
}

class _SearchViewState extends State<SearchView> {
  int pageNo = 1;
  final pageFactor = 10;
  List<Widget> getListViewWidgets(var snapshot, var query) {
    List<Widget> w = <Widget>[
      ...snapshot.data!
          .map((model) => SearchItem(
                model: model,
                query: query,
              ))
          .toList()
    ];
    if (w.isNotEmpty) {
      w = w.sublist((pageNo - 1) * pageFactor,
          (pageNo * pageFactor > w.length) ? w.length : pageNo * pageFactor);
      w.add(
        Container(
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              TextButton(
                  onPressed: (pageNo > 1)
                      ? () {
                          if (pageNo > 1) {
                            setState(() {
                              pageNo = 1;
                            });
                          }
                        }
                      : null,
                  child: Text('First page')),
              TextButton(
                  onPressed: (pageNo > 1)
                      ? () {
                          if (pageNo > 1) {
                            setState(() {
                              pageNo--;
                            });
                          }
                        }
                      : null,
                  child: Text('Previous page')),
              TextButton(
                  onPressed: () {
                    setState(() {
                      pageNo++;
                    });
                  },
                  child: Text('Next page')),
            ],
          ),
        ),
      );
    }
    return w;
  }

  @override
  Widget build(BuildContext context) {
    var query = Provider.of<SearchViewModel>(context).query;
    Provider.of<SearchViewModel>(context, listen: false).setVoice(false);
    //print(widget.pageNo);
    return FutureBuilder<dynamic>(
      future: Provider.of<SearchViewModel>(context)
          .getSearchList(query), // async work
      builder: (BuildContext context, AsyncSnapshot<dynamic> snapshot) {
        switch (snapshot.connectionState) {
          case ConnectionState.waiting:
            return ShadeLoading();
          default:
            if (snapshot.hasError)
              return Center(child: Text('Error: ${snapshot.error}'));
            else
              return Padding(
                padding: EdgeInsets.only(top: 50),
                child: ListView(
                  padding: EdgeInsets.only(top: 10),
                  children: getListViewWidgets(snapshot, query),
                ),
              );
        }
      },
    );
    /* 
    
    SingleChildScrollView(
          child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.max,
        children: <Widget>[
          SizedBox(
            height: 50,
          ),
          model.queries == null
              ? CircularProgressIndicator()
              : Wrap(
                  spacing: 30,
                  runSpacing: 30,
                  children: <Widget>[
                    ...model.queries!
                        .map((query) => SearchItem(model: query))
                        .toList()
                  ],
                ),
        ],
      )),*/
  }
}
