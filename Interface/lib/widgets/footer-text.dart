import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class FooterText extends StatelessWidget {
  final String title;
  final onPress;
  const FooterText({
    Key? key,
    required this.title,
    this.onPress,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return TextButton(
      onPressed: onPress != null ? onPress : () {},
      child: Text(
        title,
        style: GoogleFonts.poppins(
          color: Color(0xff70757a),
        ),
      ),
    );
  }
}
