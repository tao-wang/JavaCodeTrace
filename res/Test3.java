int[] values = {4, 1, 2, 3};
for (int i = values.length; i > 0; i--)
{
	for (int j = 0; j < i - 1; j++)
	{
		if (values[j] > values[j+1])
		{
			int temp = values[j+1];
			values[j+1] = values[j];
			values[j] = temp;
		}
	}
}