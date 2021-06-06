import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class SearchButton extends StatelessWidget {
  final String title;
  final onPress;

  const SearchButton({
    Key? key,
    required this.title,
    required this.onPress,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialButton(
      shape: RoundedRectangleBorder(),
      color: Color(0xffF8F9FA),
      onPressed: onPress,
      child: Text(
        title,
        style: GoogleFonts.poppins(
          color: Colors.black,
        ),
      ),
    );
  }
}
