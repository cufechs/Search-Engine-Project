import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:noogle_search_engine/dependencyInjection.dart';
import 'package:noogle_search_engine/routing/route_names.dart';
import 'package:noogle_search_engine/routing/router.dart';
// ignore: import_of_legacy_library_into_null_safe
import 'package:noogle_search_engine/services/navigation_service.dart';
import 'package:noogle_search_engine/views/layout_template.dart';
import 'dependencyInjection.dart';

void main() {
  Injector injector = new Injector();
  injector.configure(Flavor.PROD); //To be changed in the prod version
  injector.setupLocator();
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return GetMaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Noogle',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      builder: (context, child) => LayoutTemplate(
        child: child!,
      ),
      navigatorKey: locator<NavigationService>().navigatorKey,
      onGenerateRoute: generateRoute,
      initialRoute: HomeRoute,
    );
  }
}
