import 'package:get_it/get_it.dart';
import 'package:noogle_search_engine/services/api.dart';
import 'package:noogle_search_engine/services/mock_api.dart';
// ignore: import_of_legacy_library_into_null_safe
import 'package:noogle_search_engine/services/navigation_service.dart';
import 'package:noogle_search_engine/services/prod_api.dart';

GetIt locator = GetIt.instance;
enum Flavor { MOCK, PROD }

//DI
class Injector {
  //the _singleton is only created once..
  //singleton is created once the app is built
  //while lazy one is created when the Injector is called
  static final Injector _singleton = new Injector._internal();
  static Flavor? _flavor;

  void configure(Flavor flavor) {
    _flavor = flavor;
  }

  //so we used the factory constructor for that purpose not to create
  //another instance when creating another instance of Injector
  //don't forget to finish writing all the {static} objects before the factory constructor..
  factory Injector() {
    return _singleton;
  }

  Injector._internal();

  Api get api {
    switch (_flavor) {
      case Flavor.MOCK:
        return new MockApi();
      default:
        return new ProdApi();
    }
  }

  void setupLocator() {
    locator.registerLazySingleton(() => NavigationService());
    locator.registerLazySingleton(() => api);
  }
}
