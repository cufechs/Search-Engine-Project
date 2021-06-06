import 'package:flutter/material.dart';

class ShadeLoading extends StatefulWidget {
  @override
  _ShadeLoadingState createState() => _ShadeLoadingState();
}

class _ShadeLoadingState extends State<ShadeLoading>
    with SingleTickerProviderStateMixin {
  AnimationController? controllerOne;
  Animation<Color>? animationOne;
  Animation<Color>? animationTwo;

  @override
  void initState() {
    super.initState();
    controllerOne = AnimationController(
        duration: Duration(milliseconds: 2000), vsync: this);
    animationOne = ColorTween(begin: Colors.grey, end: Colors.white70)
        .animate(controllerOne!) as Animation<Color>?;
    animationTwo = ColorTween(begin: Colors.white70, end: Colors.grey)
        .animate(controllerOne!) as Animation<Color>?;
    controllerOne!.forward();
    controllerOne!.addListener(() {
      if (controllerOne!.status == AnimationStatus.completed) {
        controllerOne!.reverse();
      } else if (controllerOne!.status == AnimationStatus.dismissed) {
        controllerOne!.forward();
      }
      this.setState(() {});
    });
  }

  @override
  void dispose() {
    controllerOne!.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return ShaderMask(
      shaderCallback: (rect) {
        return LinearGradient(
                tileMode: TileMode.mirror,
                begin: Alignment.centerLeft,
                end: Alignment.centerRight,
                colors: [animationOne!.value, animationTwo!.value])
            .createShader(rect, textDirection: TextDirection.ltr);
      },
      child: Container(
        child: ListView.builder(
            itemCount: 10,
            itemBuilder: (context, index) {
              return LoadingBlock();
            }),
      ),
    );
  }
}

class LoadingBlock extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            width: double.infinity,
            height: 50,
            color: Colors.white,
          ),
          SizedBox(
            height: 8,
          ),
          Container(
            width: double.infinity,
            height: 8.0,
            color: Colors.white,
          ),
          SizedBox(
            height: 4,
          ),
          Container(
            width: double.infinity,
            height: 8.0,
            color: Colors.white,
          ),
          SizedBox(
            height: 4,
          ),
          Container(
            width: 20.0,
            height: 8.0,
            color: Colors.white,
          ),
        ],
      ),
    );
  }
}
