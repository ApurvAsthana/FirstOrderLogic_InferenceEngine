import java.util.ArrayList;

public class Term {
		String predicate;
		ArrayList<String> arguements;
		boolean isNegated;

		public Term() {
			super();
			this.arguements = new ArrayList<>();
		}

		public Term(String s) {
			super();
			if (s.contains("~")) {
				this.isNegated = true;
				s = s.substring(1, s.length());
			}
			this.predicate = s.substring(0, s.indexOf("(")).trim();
			this.arguements = new ArrayList<>();
			String innerString;
			innerString = s.substring(s.indexOf("(") + 1, s.indexOf(")")).trim();
			String[] itemsInInnerString = innerString.split(",");
			for (int j = 0; j < itemsInInnerString.length; j++) {
				this.arguements.add(itemsInInnerString[j].trim());
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((arguements == null) ? 0 : arguements.hashCode());
			result = prime * result + (isNegated ? 1231 : 1237);
			result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Term other = (Term) obj;
			if (arguements == null) {
				if (other.arguements != null)
					return false;
			} else if (!arguements.equals(other.arguements))
				return false;
			if (isNegated != other.isNegated)
				return false;
			if (predicate == null) {
				if (other.predicate != null)
					return false;
			} else if (!predicate.equals(other.predicate))
				return false;
			return true;
		}
	}