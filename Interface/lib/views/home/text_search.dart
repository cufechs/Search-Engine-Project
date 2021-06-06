import 'package:flutter/material.dart';
import 'package:material_floating_search_bar/material_floating_search_bar.dart';
import 'package:noogle_search_engine/viewmodels/search_queries_view_model.dart';
import 'package:noogle_search_engine/views/search_view/search_view.dart';
// ignore: import_of_legacy_library_into_null_safe
import 'package:provider/provider.dart';
// ignore: import_of_legacy_library_into_null_safe
import 'package:shared_preferences/shared_preferences.dart';

class TextView extends StatefulWidget {
  @override
  _TextViewState createState() => _TextViewState();
}

class _TextViewState extends State<TextView> {
  List<String> _searchHistory = [];
  List<String> filteredSearchHistory = [];
  bool voiceReload = false;
  Future _getList() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    List<String> searchHistory2 = prefs.getStringList('history') ?? [];
    setState(() {
      _searchHistory = searchHistory2;
      filteredSearchHistory = searchHistory2;
      voiceReload = true;
    });
    print(_searchHistory);
  }

  _setList() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    await prefs.setStringList('history', _searchHistory).then((value) {
      print('Saved');
    });
  }

  static const historyLength = 5;
  bool search = false;

  String selectedTerm = '';

  List<String> filterSearchTerms({
    required String filter,
  }) {
    // nnnn
    if (filter.isNotEmpty) {
      return _searchHistory.reversed
          .where((term) => term.startsWith(filter))
          .toList();
    } else {
      return _searchHistory.reversed.toList();
    }
  }

  void addSearchTerm(String term) {
    if (_searchHistory.contains(term)) {
      putSearchTermFirst(term);
      return;
    }

    _searchHistory.add(term);
    if (_searchHistory.length > historyLength) {
      _searchHistory.removeRange(0, _searchHistory.length - historyLength);
    }

    filteredSearchHistory = filterSearchTerms(filter: '');
  }

  void deleteSearchTerm(String term) {
    _searchHistory.removeWhere((t) => t == term);
    filteredSearchHistory = filterSearchTerms(filter: '');
  }

  void putSearchTermFirst(String term) {
    deleteSearchTerm(term);
    addSearchTerm(term);
  }

  late FloatingSearchBarController controller;

  Widget searchWidget = Container();

  @override
  void initState() {
    super.initState();
    print('Init state!!');
    Future.delayed(Duration.zero, () async {
      await _getList();
    });

    controller = FloatingSearchBarController();
    filteredSearchHistory = filterSearchTerms(filter: '');
  }

  Widget update(bool voiceSearch, var query) {
    print('Update function!!');
    if (search || voiceSearch) {
      searchWidget = SearchView();
    }
    if (voiceReload && query.isNotEmpty) {
      setState(() {
        addSearchTerm(query);
        print(filteredSearchHistory);
        selectedTerm = query;
        voiceReload = false;
      });
    }
    return searchWidget;
  }

  @override
  void dispose() {
    Future.delayed(Duration.zero, () async {
      await _setList();
    });
    controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    bool voiceSearch = Provider.of<SearchViewModel>(context).voiceSearch;
    var query = Provider.of<SearchViewModel>(context).query;
    return Scaffold(
      body: SafeArea(
        child: FloatingSearchBar(
          controller: controller,
          body: FloatingSearchBarScrollNotifier(
            child: update(voiceSearch, query),
          ),
          transition: CircularFloatingSearchBarTransition(),
          isScrollControlled: false,
          physics: BouncingScrollPhysics(),
          title: Text(
            selectedTerm,
            style: Theme.of(context).textTheme.headline6,
          ),
          hint: 'Search and find out...',
          actions: [
            FloatingSearchBarAction.searchToClear(),
          ],
          onQueryChanged: (query) {
            setState(() {
              search = false;
              filteredSearchHistory = filterSearchTerms(filter: query);
            });
          },
          onSubmitted: (query) {
            if (query.isNotEmpty) {
              print("onSubmitted - $query");
              setState(() {
                Provider.of<SearchViewModel>(context, listen: false)
                    .setQuery(query);
                addSearchTerm(query);
                selectedTerm = query;
                search = true;
              });
              controller.close();
            }
          },
          builder: (context, transition) {
            return ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: Material(
                color: Colors.white,
                elevation: 4,
                child: Builder(
                  builder: (context) {
                    if ((filteredSearchHistory.isEmpty ||
                            (filteredSearchHistory.length == 1 &&
                                filteredSearchHistory[0].isEmpty)) &&
                        controller.query.isEmpty) {
                      return Container(
                        height: 56,
                        width: double.infinity,
                        alignment: Alignment.center,
                        child: Text(
                          'Start searching',
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: Theme.of(context).textTheme.caption,
                        ),
                      );
                    } else if (filteredSearchHistory.isEmpty ||
                        (filteredSearchHistory.length == 1 &&
                            filteredSearchHistory[0].isEmpty)) {
                      return ListTile(
                        title: Text(controller.query),
                        leading: const Icon(Icons.search),
                        onTap: () {
                          print('add..');
                          setState(() {
                            Provider.of<SearchViewModel>(context, listen: false)
                                .setQuery(controller.query);
                            search = true;
                            addSearchTerm(controller.query);
                            selectedTerm = controller.query;
                          });
                          controller.close();
                        },
                      );
                    } else {
                      print(filteredSearchHistory);
                      return Column(
                        mainAxisSize: MainAxisSize.min,
                        children: filteredSearchHistory
                            .map(
                              (term) => ListTile(
                                title: Text(
                                  term,
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                                leading: const Icon(Icons.history),
                                trailing: IconButton(
                                  icon: const Icon(Icons.clear),
                                  onPressed: () {
                                    setState(() {
                                      search = false;
                                      print("onPressed - $term");
                                      deleteSearchTerm(term);
                                    });
                                  },
                                ),
                                onTap: () {
                                  print("onTap - $term");
                                  setState(() {
                                    Provider.of<SearchViewModel>(context,
                                            listen: false)
                                        .setQuery(term);
                                    search = true;
                                    putSearchTermFirst(term);
                                    selectedTerm = term;
                                  });
                                  controller.close();
                                },
                              ),
                            )
                            .toList(),
                      );
                    }
                  },
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}
