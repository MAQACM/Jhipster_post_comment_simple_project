import dayjs from 'dayjs/esm';
import { IPost } from 'app/entities/post/post.model';

export interface IComment {
  id?: string;
  text?: string | null;
  creaionDate?: dayjs.Dayjs;
  post?: IPost;
}

export class Comment implements IComment {
  constructor(public id?: string, public text?: string | null, public creaionDate?: dayjs.Dayjs, public post?: IPost) {}
}

export function getCommentIdentifier(comment: IComment): string | undefined {
  return comment.id;
}
