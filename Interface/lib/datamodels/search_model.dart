class SearchQueryModel {
  final String? title;
  final String? link;
  final String? description;

  SearchQueryModel({
    this.title,
    this.link,
    this.description,
  });

  SearchQueryModel.fromJson(Map<String, dynamic> map)
      : title = map['title'],
        link = map['link'],
        description = map['description'];
}
