import 'package:flutter/material.dart';
import 'package:noogle_search_engine/datamodels/search_model.dart';
import 'package:noogle_search_engine/routing/route_names.dart';
// ignore: import_of_legacy_library_into_null_safe
import 'package:noogle_search_engine/services/navigation_service.dart';
import '../../dependencyInjection.dart';
import '../../widgets/google-text.dart';
import '../../widgets/language-text.dart';

import 'package:http/http.dart' as http;
import 'dart:convert';

class HomeView extends StatefulWidget {
  @override
  _HomeViewState createState() => _HomeViewState();
}

class _HomeViewState extends State<HomeView> {
  @override
  Widget build(BuildContext context) {
    return Center(
      child: SingleChildScrollView(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.start,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                GoogleText(),
              ],
            ),
            SizedBox(height: 20),
            Container(
              decoration: BoxDecoration(
                border: Border.all(),
                borderRadius: BorderRadius.circular(30),
              ),
              child: ListTile(
                onTap: () {
                  locator<NavigationService>().navigateTo(TextRoute);
                },
                title: Text('Search Noogle..'),
                trailing: InkWell(
                  onTap: () {
                    locator<NavigationService>().navigateTo(SpeechRoute);
                  },
                  child: Image.asset(
                    'assets/icons/speech.png',
                    height: 24,
                    width: 24,
                  ),
                ),
                leading: Icon(Icons.search),
              ),
              /*: InputDecoration(
                  hintText: 'Search Noogle',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(30)),
                  ),
                  prefixIcon: Icon(Icons.search),
                  suffixIcon: Padding(
                    padding: EdgeInsets.all(8),
                    child: InkWell(
                      onTap: () {
                        locator<NavigationService>().navigateTo(SpeechRoute);
                      },
                      child: Image.asset(
                        'assets/icons/speech.png',
                        height: 24,
                        width: 24,
                      ),
                    ),
                  ),
                ),,*/
              width: (MediaQuery.of(context).size.width <= 500)
                  ? MediaQuery.of(context).size.width * 0.7
                  : ((MediaQuery.of(context).size.width <= 1023)
                      ? MediaQuery.of(context).size.width * 0.55
                      : (MediaQuery.of(context).size.width * 0.5 < 650)
                          ? MediaQuery.of(context).size.width * 0.5
                          : 650),
            ),
            // Row(
            //   mainAxisAlignment: MainAxisAlignment.center,
            //   children: [
            //     Container(
            //       child: Row(
            //         mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            //         children: [
            //           SearchButton(
            //             title: 'Noogle Search',
            //             onPress: () {
            //               locator<NavigationService>().navigateTo(SearchRoute);
            //             },
            //           ),
            //           SizedBox(width: 10),
            //           SearchButton(
            //             title: "I'm Feeling Lucky",
            //             onPress: () {
            //               locator<NavigationService>().navigateTo(SearchRoute);
            //             },
            //           ),
            //         ],
            //       ),
            //     ),
            //   ],
            // ),
            SizedBox(height: 30),
            Wrap(
              alignment: WrapAlignment.center,
              children: [
                Text('Noogle offered in: '),
                SizedBox(width: 5),
                LanguageText(title: 'بالعربية'),
                SizedBox(width: 5),
                Text(' "Actually Not :D " '),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
