import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:noogle_search_engine/routing/route_names.dart';
import 'package:noogle_search_engine/views/home/text_search.dart';
import 'package:noogle_search_engine/views/home/voice_search.dart';
import 'package:noogle_search_engine/views/home/home_view.dart';
import 'package:noogle_search_engine/views/search_view/search_view.dart';

Route<dynamic> generateRoute(RouteSettings settings) {
  switch (settings.name) {
    case HomeRoute:
      return _getPageRoute(HomeView(), settings);
    case SearchRoute:
      return _getPageRoute(SearchView(), settings);
    case SpeechRoute:
      return _getPageRoute(SpeechView(), settings);
    case TextRoute:
      return _getPageRoute(TextView(), settings);
    default:
      return _getPageRoute(HomeView(), settings);
  }
}

PageRoute _getPageRoute(Widget child, RouteSettings settings) {
  return _FadeRoute(child: child, routeName: settings.name!);
}

class _FadeRoute extends PageRouteBuilder {
  final Widget? child;
  final String? routeName;
  _FadeRoute({this.child, this.routeName})
      : super(
          settings: RouteSettings(name: routeName),
          pageBuilder: (
            BuildContext context,
            Animation<double> animation,
            Animation<double> secondaryAnimation,
          ) =>
              child!,
          transitionsBuilder: (
            BuildContext context,
            Animation<double> animation,
            Animation<double> secondaryAnimation,
            Widget child,
          ) =>
              FadeTransition(
            opacity: animation,
            child: child,
          ),
        );
}
