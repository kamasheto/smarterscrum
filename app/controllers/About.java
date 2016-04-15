package controllers;

/**
 * About controller, handles all about us requests
 */
public class About extends SmartController
{
	/**
	 * Renders the default about section.
	 * 
	 * @see An image of the smartsoft team, with a title for each developer.
	 */
	public static void index()
	{
		String[] data = { "Amr Hany (C2), the CRUD guru! Amr, the man behind the chat system, is akbar men keda bekter", "Ahmed Khaled (C4). Ask him anything and he'll say: &#147;fol&#148;", "Mina Zaki (C2). Mina introduced us to the accordion. Things have become musical even since then.", "Mahmoud Sharawi (C3)", "Joseph ElHajj (C5). Joe speaks Japanese, and watches cartoons. (Even during meetings!)", "Hossam Sharaf (C2)", "Mohamed Monayri (C3). One of the people behind the estimation game!", "Moataz Mekki (C1). Moataz is notifying everyone!", "Omar Nabil (C1)", "Hossam Amer (C4). Hossam was working on the review log. We still don't know what that is. Just kidding. We do. Really. (Just don't ask.)", "Amr Othman (C1). Wallas logs everything.", "Moumen Mohamed (C3). Moumen took care of task types. He's also very tall. Really really tall.", "Galal Aly (C3-lead). Galal is also one of the people behind the estimation game. He's also mesh moktane3.", "Ahmed Behairy (C2). He approves project requests. Really.", "Saher (scrum-master). He made sure we followed the rules. He also brought cake. Lots and lots of it.", "Amr Abdelwahab (C5). Amr took snapshots. During meetings, after sprints. In fact, he still is taking snapshots.", "Mahmoud Sakr (C1-lead, Manager). Mahmoud is also known as ela2ra3. Unfortunately, it is true.", "Menna Ghoneim (C4-lead). You'll never find the AC off when she's around! She wouldn't mind the food going cold, too.", "Dina Helal (C5-lead). Dina dragged and dropped. She would have dropped a rock on cartoons if she could.", "Heba Sherif (C3). Heba, our best reviewer, loves sequence diagrams", //
		"Hadeer Diwan (C5). Hadeer laid down the board back-end structure. She didn't miss a chance to get mad at cartoons, too.", "Hadeer Younis (C4). Hadeer made our site look pretty. She's also very strong indeed!", "Dr Fatma Meawad (CEO). Dr Fatma didn't only request a whole lot, she also made us change it all (A)", "Asmaa Alkomy (C5). Asmaa made dish parties come true. She is really in C5. I promise.", "Ghada Fakhry (C2-lead). Ghada had to put up with ALL our ERD issues. She also has the best ringtone ever!", };
		render( null, data );
	}
}
